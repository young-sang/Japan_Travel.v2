package com.japantravel.recommendation;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.prefecture.repository.PrefectureRepository;
import com.japantravel.recommendation.ai.AiDailyLimiter;
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
    @Mock
    private AiDailyLimiter limiter;

    private AiRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new AiRecommendationService(repository, prefectureRepository, llmPicker, limiter, 150, 80);
        lenient().when(limiter.tryAcquire()).thenReturn(true);
        lenient().when(prefectureRepository.existsByName(PREF)).thenReturn(true);
        lenient().when(repository.findDestinations(PREF, 200, 150)).thenReturn(List.of(
                dest(1), dest(2), dest(3), dest(4), dest(5)));
//      축제 1번은 여행지 1번과 id 가 같다
        lenient().when(repository.findFestivals(eq(PREF), anyList(), eq(200), eq(150)))
                .thenReturn(List.of(fest(1)));
        lenient().when(repository.findCardInfo(anyList(), anyList())).thenReturn(List.of());
    }

    private static PlaceCandidate dest(long id) {
        return new PlaceCandidate(RecommendationTargetType.DESTINATION, id, "장소" + id, PREF, null, "설명" + id);
    }

    private static PlaceCandidate fest(long id) {
        return new PlaceCandidate(RecommendationTargetType.FESTIVAL, id, "축제" + id, PREF, null, "축제 설명" + id);
    }

//  LLM 이 고른 한 곳 — 이름은 그 키의 후보 이름 그대로 (dest · fest 와 같은 규칙)
    private static LlmPicker.Pick p(String key, String reason) {
        return new LlmPicker.Pick(key, (key.startsWith("D") ? "장소" : "축제") + key.substring(1), reason);
    }

    private List<AiRecommendationResponse> recommend(AiPlaceKind kind, List<LlmPicker.Pick> picks) {
        given(llmPicker.pick(anyString(), any(), anyList())).willReturn(picks);
        return service.recommend(new AiRecommendationRequest("조용한 온천", PREF, kind, null));
    }

    @Test
    void 후보_밖_키는_버리고_중복은_처음_것만() {
        List<AiRecommendationResponse> result = recommend(AiPlaceKind.DESTINATION, List.of(
                p("D99", "후보 밖"), p("D2", "첫째"),
                p("D2", "중복"), p("D4", "둘째")));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(2L, 4L);
        assertThat(result).extracting(AiRecommendationResponse::reason).containsExactly("첫째", "둘째");
    }

    @Test
    void 세_곳을_넘으면_자른다() {
        List<AiRecommendationResponse> result = recommend(AiPlaceKind.DESTINATION, List.of(
                p("D1", "a"), p("D2", "b"),
                p("D3", "c"), p("D4", "d")));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(1L, 2L, 3L);
    }

    @Test
    void 여행지와_축제의_id가_같아도_키로_구분한다() {
        List<AiRecommendationResponse> result = recommend(AiPlaceKind.ALL, List.of(p("F1", "축제")));
        assertThat(result).singleElement().satisfies(r -> {
            assertThat(r.type()).isEqualTo(RecommendationTargetType.FESTIVAL);
            assertThat(r.targetId()).isEqualTo(1L);
            assertThat(r.name()).isEqualTo("축제1");
        });
    }

    @Test
    void 여행지만이면_축제를_조회하지_않고_축제_키도_버린다() {
        assertThatThrownBy(() -> recommend(AiPlaceKind.DESTINATION, List.of(p("F1", "축제"))))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AI_RECOMMEND_FAILED);
        verify(repository, never()).findFestivals(any(), anyList(), anyInt(), anyInt());
    }

    @Test
    void 계절을_고르면_축제는_그_달로_거르고_LLM에_계절을_넘긴다() {
        given(llmPicker.pick(anyString(), any(), anyList())).willReturn(List.of(p("F1", "가을")));
        service.recommend(new AiRecommendationRequest("단풍", PREF, AiPlaceKind.FESTIVAL, Season.AUTUMN));
        verify(repository).findFestivals(PREF, List.of(9, 10, 11), 200, 150);
        verify(llmPicker).pick(eq("단풍"), eq("가을(9~11월)"), anyList());
        verify(repository, never()).findDestinations(any(), anyInt(), anyInt());
    }

    @Test
    void 종류_계절을_생략하면_둘_다_상관없음() {
        given(llmPicker.pick(anyString(), isNull(), anyList())).willReturn(List.of(p("D1", "a")));
        service.recommend(new AiRecommendationRequest("조용한 온천", PREF, null, null));
        verify(repository).findDestinations(PREF, 200, 150);
        verify(repository).findFestivals(PREF, List.of(), 200, 150);
    }

    @Test
    void 검증_후_0곳이면_실패() {
        assertThatThrownBy(() -> recommend(AiPlaceKind.ALL, List.of(p("D99", "후보 밖"))))
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
        given(repository.findDestinations(PREF, 200, 150)).willReturn(List.of());
        assertThatThrownBy(() -> service.recommend(
                new AiRecommendationRequest("조용한 온천", PREF, AiPlaceKind.DESTINATION, null)))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NO_AI_CANDIDATES);
        verify(llmPicker, never()).pick(anyString(), any(), anyList());
//      후보 0곳은 하루 상한에 세지 않는다 (M4-4)
        verify(limiter, never()).tryAcquire();
    }

    @Test
    void 하루_상한을_넘으면_LLM을_부르지_않는다() {
        given(limiter.tryAcquire()).willReturn(false);
        assertThatThrownBy(() -> service.recommend(new AiRecommendationRequest("조용한 온천", PREF, null, null)))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AI_DAILY_LIMIT_EXCEEDED);
        verify(llmPicker, never()).pick(anyString(), any(), anyList());
    }

    @Test
    void 입력_오류는_하루_상한에_세지_않는다() {
        assertThatThrownBy(() -> service.recommend(new AiRecommendationRequest(" ", PREF, null, null)))
                .isInstanceOf(ApiException.class);
        verify(limiter, never()).tryAcquire();
    }

    @Test
    void 검증_후_1곳만_남으면_카드_1장() {
        List<AiRecommendationResponse> result = recommend(AiPlaceKind.DESTINATION, List.of(
                p("D99", "후보 밖"), p("D3", "하나")));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(3L);
    }

    @Test
    void 지역이_없으면_전국_후보를_짧은_설명으로_상한_없이_모은다() {
        given(repository.findDestinations(null, 80, Integer.MAX_VALUE)).willReturn(List.of(dest(7)));
        given(repository.findFestivals(null, List.of(), 80, Integer.MAX_VALUE)).willReturn(List.of(fest(8)));
        given(llmPicker.pick(anyString(), isNull(), anyList())).willReturn(List.of(p("D7", "전국")));
        List<AiRecommendationResponse> result =
                service.recommend(new AiRecommendationRequest("한적한 온천", null, null, null));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(7L);
        verify(prefectureRepository, never()).existsByName(anyString());
    }

    @Test
    void 빈_지역_문자열도_전국이다() {
        given(repository.findDestinations(null, 80, Integer.MAX_VALUE)).willReturn(List.of(dest(7)));
        given(llmPicker.pick(anyString(), isNull(), anyList())).willReturn(List.of(p("D7", "전국")));
        service.recommend(new AiRecommendationRequest("한적한 온천", " ", AiPlaceKind.DESTINATION, null));
        verify(repository).findDestinations(null, 80, Integer.MAX_VALUE);
    }

    @Test
    void LLM이_맞는_곳이_없다고_하면_NO_AI_MATCH() {
        assertThatThrownBy(() -> recommend(AiPlaceKind.DESTINATION, List.of()))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NO_AI_MATCH);
    }

    @Test
    void 키와_이름이_다르면_버린다() {
//      D1 에 D2 의 이름을 붙임 (측정에서 본 "이유가 다른 장소에 붙는" 경우) → 버리고 D3 만 남음
        List<AiRecommendationResponse> result = recommend(AiPlaceKind.DESTINATION, List.of(
                new LlmPicker.Pick("D1", "장소2", "엉뚱한 이유"), p("D3", "맞음")));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(3L);
    }

    @Test
    void 이름은_공백만_달라도_같다고_본다() {
        List<AiRecommendationResponse> result = recommend(AiPlaceKind.DESTINATION, List.of(
                new LlmPicker.Pick("D2", " 장 소2 ", "띄어쓰기")));
        assertThat(result).extracting(AiRecommendationResponse::targetId).containsExactly(2L);
    }
}
