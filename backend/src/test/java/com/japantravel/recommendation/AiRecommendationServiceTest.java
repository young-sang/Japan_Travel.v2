package com.japantravel.recommendation;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.prefecture.repository.PrefectureRepository;
import com.japantravel.recommendation.ai.LlmPicker;
import com.japantravel.recommendation.dto.AiPlaceKind;
import com.japantravel.recommendation.dto.AiRecommendationRequest;
import com.japantravel.recommendation.dto.AiRecommendationResponse;
import com.japantravel.recommendation.dto.RecommendationTargetType;
import com.japantravel.recommendation.dto.Season;
import com.japantravel.recommendation.repository.RecommendationRepository;
import com.japantravel.recommendation.repository.RecommendationRepository.PlaceCandidate;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

//  AI 추천의 LLM 응답 검증 — 후보 안의 키만, 중복 제거, 최대 3곳 (M4-1), 여행지 · 축제 id 충돌 방지 (M4-2)
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
                dest(1), dest(2), dest(3), dest(4), dest(5)));
//      축제 1번은 여행지 1번과 id 가 같다
        lenient().when(repository.findFestivalsByPrefecture(eq(PREF), anyList(), eq(150)))
                .thenReturn(List.of(fest(1)));
        lenient().when(repository.findCardInfo(anyList(), anyList())).thenReturn(List.of());
    }

    private static PlaceCandidate dest(long id) {
        return new PlaceCandidate(RecommendationTargetType.DESTINATION, id, "장소" + id, PREF, null, "설명" + id);
    }

    private static PlaceCandidate fest(long id) {
        return new PlaceCandidate(RecommendationTargetType.FESTIVAL, id, "축제" + id, PREF, null, "축제 설명" + id);
    }

    private List<AiRecommendationResponse> recommend(AiPlaceKind kind, List<LlmPicker.Pick> picks) {
        given(llmPicker.pick(anyString(), any(), anyList())).willReturn(picks);
        return service.recommend(new AiRecommendationRequest("조용한 온천", PREF, kind, null));
    }

    @Test
    void 후보_밖_키는_버리고_중복은_처음_것만() {
        List<AiRecommendationResponse> result = recommend(AiPlaceKind.DESTINATION, List.of(
                new LlmPicker.Pick("D99", "후보 밖"), new LlmPicker.Pick("D2", "첫째"),
                new LlmPicker.Pick("D2", "중복"), new LlmPicker.Pick("D4", "둘째")));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(2L, 4L);
        assertThat(result).extracting(AiRecommendationResponse::reason).containsExactly("첫째", "둘째");
    }

    @Test
    void 세_곳을_넘으면_자른다() {
        List<AiRecommendationResponse> result = recommend(AiPlaceKind.DESTINATION, List.of(
                new LlmPicker.Pick("D1", "a"), new LlmPicker.Pick("D2", "b"),
                new LlmPicker.Pick("D3", "c"), new LlmPicker.Pick("D4", "d")));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(1L, 2L, 3L);
    }

    @Test
    void 여행지와_축제의_id가_같아도_키로_구분한다() {
        List<AiRecommendationResponse> result = recommend(AiPlaceKind.ALL, List.of(new LlmPicker.Pick("F1", "축제")));
        assertThat(result).singleElement().satisfies(r -> {
            assertThat(r.type()).isEqualTo(RecommendationTargetType.FESTIVAL);
            assertThat(r.targetId()).isEqualTo(1L);
            assertThat(r.name()).isEqualTo("축제1");
        });
    }

    @Test
    void 여행지만이면_축제를_조회하지_않고_축제_키도_버린다() {
        assertThatThrownBy(() -> recommend(AiPlaceKind.DESTINATION, List.of(new LlmPicker.Pick("F1", "축제"))))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AI_RECOMMEND_FAILED);
        verify(repository, never()).findFestivalsByPrefecture(anyString(), anyList(), anyInt());
    }

    @Test
    void 계절을_고르면_축제는_그_달로_거르고_LLM에_계절을_넘긴다() {
        given(llmPicker.pick(anyString(), any(), anyList())).willReturn(List.of(new LlmPicker.Pick("F1", "가을")));
        service.recommend(new AiRecommendationRequest("단풍", PREF, AiPlaceKind.FESTIVAL, Season.AUTUMN));
        verify(repository).findFestivalsByPrefecture(PREF, List.of(9, 10, 11), 150);
        verify(llmPicker).pick(eq("단풍"), eq("가을(9~11월)"), anyList());
        verify(repository, never()).findDestinationsByPrefecture(anyString(), anyInt());
    }

    @Test
    void 종류_계절을_생략하면_둘_다_상관없음() {
        given(llmPicker.pick(anyString(), isNull(), anyList())).willReturn(List.of(new LlmPicker.Pick("D1", "a")));
        service.recommend(new AiRecommendationRequest("조용한 온천", PREF, null, null));
        verify(repository).findDestinationsByPrefecture(PREF, 150);
        verify(repository).findFestivalsByPrefecture(PREF, List.of(), 150);
    }

    @Test
    void 검증_후_0곳이면_실패() {
        assertThatThrownBy(() -> recommend(AiPlaceKind.ALL, List.of(new LlmPicker.Pick("D99", "후보 밖"))))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AI_RECOMMEND_FAILED);
    }

    @Test
    void 취향_문장이_200자를_넘으면_400() {
        assertThatThrownBy(() -> service.recommend(new AiRecommendationRequest("가".repeat(201), PREF, null, null)))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_AI_REQUEST);
    }

    @Test
    void 후보가_없으면_NO_AI_CANDIDATES_이고_LLM을_부르지_않는다() {
        given(repository.findDestinationsByPrefecture(PREF, 150)).willReturn(List.of());
        assertThatThrownBy(() -> service.recommend(
                new AiRecommendationRequest("조용한 온천", PREF, AiPlaceKind.DESTINATION, null)))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NO_AI_CANDIDATES);
        verify(llmPicker, never()).pick(anyString(), any(), anyList());
    }

    @Test
    void 검증_후_1곳만_남으면_카드_1장() {
        List<AiRecommendationResponse> result = recommend(AiPlaceKind.DESTINATION, List.of(
                new LlmPicker.Pick("D99", "후보 밖"), new LlmPicker.Pick("D3", "하나")));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(3L);
    }
}
