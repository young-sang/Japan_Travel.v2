package com.japantravel.recommendation;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.prefecture.repository.PrefectureRepository;
import com.japantravel.recommendation.ai.LlmPicker;
import com.japantravel.recommendation.dto.AiRecommendationRequest;
import com.japantravel.recommendation.dto.AiRecommendationResponse;
import com.japantravel.recommendation.repository.RecommendationRepository;
import com.japantravel.recommendation.repository.RecommendationRepository.DestinationCandidate;
import com.japantravel.recommendation.service.AiRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

//  AI 추천의 LLM 응답 검증 (M4-1) — 후보 안의 id 만, 중복 제거, 최대 3곳
@ExtendWith(MockitoExtension.class)
class AiRecommendationServiceTest {

    private static final String PREF = "오이타현";

    @Mock
    private RecommendationRepository repository;
    @Mock
    private PrefectureRepository prefectureRepository;
    @Mock
    private LlmPicker llmPicker;

    private AiRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new AiRecommendationService(repository, prefectureRepository, llmPicker, 150);
        lenient().when(prefectureRepository.existsByName(PREF)).thenReturn(true);
        lenient().when(repository.findDestinationsByPrefecture(PREF, 150)).thenReturn(List.of(
                candidate(1), candidate(2), candidate(3), candidate(4), candidate(5)));
        lenient().when(repository.findCardInfo(anyList(), anyList())).thenReturn(List.of());
    }

    private static DestinationCandidate candidate(long id) {
        return new DestinationCandidate(id, "장소" + id, PREF, null, "설명" + id);
    }

    private List<AiRecommendationResponse> recommend(List<LlmPicker.Pick> picks) {
        given(llmPicker.pick(anyString(), any())).willReturn(picks);
        return service.recommend(new AiRecommendationRequest("조용한 온천", PREF));
    }

    @Test
    void 후보_밖_id는_버리고_중복은_처음_것만() {
        List<AiRecommendationResponse> result = recommend(List.of(
                new LlmPicker.Pick(99, "후보 밖"), new LlmPicker.Pick(2, "첫째"),
                new LlmPicker.Pick(2, "중복"), new LlmPicker.Pick(4, "둘째")));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(2L, 4L);
        assertThat(result).extracting(AiRecommendationResponse::reason).containsExactly("첫째", "둘째");
    }

    @Test
    void 세_곳을_넘으면_자른다() {
        List<AiRecommendationResponse> result = recommend(List.of(
                new LlmPicker.Pick(1, "a"), new LlmPicker.Pick(2, "b"),
                new LlmPicker.Pick(3, "c"), new LlmPicker.Pick(4, "d")));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(1L, 2L, 3L);
    }

    @Test
    void 검증_후_0곳이면_실패() {
        assertThatThrownBy(() -> recommend(List.of(new LlmPicker.Pick(99, "후보 밖"))))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AI_RECOMMEND_FAILED);
    }

    @Test
    void 취향_문장이_200자를_넘으면_400() {
        assertThatThrownBy(() -> service.recommend(new AiRecommendationRequest("가".repeat(201), PREF)))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_AI_REQUEST);
    }

    @Test
    void 후보가_없으면_실패() {
        given(repository.findDestinationsByPrefecture(PREF, 150)).willReturn(List.of());
        assertThatThrownBy(() -> service.recommend(new AiRecommendationRequest("조용한 온천", PREF)))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AI_RECOMMEND_FAILED);
    }
}
