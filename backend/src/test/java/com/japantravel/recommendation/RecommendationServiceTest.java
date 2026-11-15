package com.japantravel.recommendation;

import com.japantravel.recommendation.dto.ActivityKind;
import com.japantravel.recommendation.dto.RecommendationResponse;
import com.japantravel.recommendation.repository.RecommendationRepository;
import com.japantravel.recommendation.repository.RecommendationRepository.Activity;
import com.japantravel.recommendation.repository.RecommendationRepository.CardInfo;
import com.japantravel.recommendation.repository.RecommendationRepository.Place;
import com.japantravel.recommendation.repository.RecommendationRepository.Rating;
import com.japantravel.recommendation.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.japantravel.recommendation.dto.RecommendationTargetType.DESTINATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

//  별점 우대 · 낮은 별점 (D-103). 2차원 벡터로 유사도를 손으로 맞춘다
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    RecommendationRepository repository;

    @InjectMocks
    RecommendationService service;

//  길이 1 벡터. 각도로 유사도를 정한다 — (1, 0) 과의 코사인 = cos(deg)
    private static Place place(long id, String prefecture, double deg) {
        double r = Math.toRadians(deg);
        return new Place(DESTINATION, id, "장소" + id, prefecture, null, new float[]{(float) Math.cos(r), (float) Math.sin(r)});
    }

    private static Activity favorite(long id) {
        return new Activity(DESTINATION, id, ActivityKind.FAVORITE, false);
    }

    private static Activity disliked(long id) {
        return new Activity(DESTINATION, id, ActivityKind.REVIEW, true);
    }

    @Test
    void 낮은_별점_리뷰만_있으면_빈_결과() {
        when(repository.findActivities(1L)).thenReturn(List.of(disliked(1)));
        when(repository.findAllPlaces()).thenReturn(List.of(place(1, "A현", 0), place(2, "B현", 10)));

        assertThat(service.recommend(1L)).isEmpty();
    }

    @Test
    void 낮은_별점_장소는_근거가_아니다() {
//      1 을 좋아하고 2 를 싫어한다. 2 와 가까운 후보도 근거는 1 이다
        when(repository.findActivities(1L)).thenReturn(List.of(favorite(1), disliked(2)));
        when(repository.findAllPlaces()).thenReturn(List.of(
                place(1, "A현", 0), place(2, "B현", 90),
                place(3, "C현", 5), place(4, "D현", 80), place(5, "E현", 30)));
        lenient().when(repository.findRatings()).thenReturn(List.of());

        List<RecommendationResponse> result = service.recommend(1L);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(r -> r.basis().targetId() == 1L);
//      싫어한 2 와 가까운 4 는 좋아한 1 과 가까운 3 보다 뒤에 온다
        assertThat(result.get(0).targetId()).isEqualTo(3L);
    }

    @Test
    void 리뷰_2건_이상인_곳의_높은_별점이_비슷한_후보의_순서를_바꾼다() {
        when(repository.findActivities(1L)).thenReturn(List.of(favorite(1)));
//      3 은 cos(10°) ≈ 0.985, 4 는 cos(13°) ≈ 0.974 — 0.011 차이. 4 가 평균 5점이면 +0.02 로 앞선다
        when(repository.findAllPlaces()).thenReturn(List.of(place(1, "A현", 0), place(3, "C현", 10), place(4, "D현", 13)));
        when(repository.findRatings()).thenReturn(List.of(new Rating(DESTINATION, 4, 5.0)));

        List<RecommendationResponse> result = service.recommend(1L);

        assertThat(result).extracting(RecommendationResponse::targetId).containsExactly(4L, 3L);
//      응답의 similarity 는 보너스를 더하지 않은 원래 값
        assertThat(result.get(0).similarity()).isLessThan(result.get(1).similarity());
    }

    @Test
    void 별점이_없으면_유사도_순서_그대로() {
        when(repository.findActivities(1L)).thenReturn(List.of(favorite(1)));
        when(repository.findAllPlaces()).thenReturn(List.of(place(1, "A현", 0), place(3, "C현", 10), place(4, "D현", 13)));
        when(repository.findRatings()).thenReturn(List.of());

        assertThat(service.recommend(1L)).extracting(RecommendationResponse::targetId).containsExactly(3L, 4L);
    }

    @Test
    void 고른_곳에_카드_정보를_붙이고_없으면_빈값() {
        when(repository.findActivities(1L)).thenReturn(List.of(favorite(1)));
        when(repository.findAllPlaces()).thenReturn(List.of(place(1, "A현", 0), place(3, "C현", 10), place(4, "D현", 13)));
        when(repository.findRatings()).thenReturn(List.of());
//      3 만 정보가 있다. 4 는 못 찾은 경우
        when(repository.findCardInfo(List.of(3L, 4L), List.of()))
                .thenReturn(List.of(new CardInfo(DESTINATION, 3, "설명3", 4.5, 2)));

        List<RecommendationResponse> result = service.recommend(1L);

        assertThat(result.get(0).description()).isEqualTo("설명3");
        assertThat(result.get(0).avgRating()).isEqualTo(4.5);
        assertThat(result.get(0).reviewCount()).isEqualTo(2);
        assertThat(result.get(1).description()).isNull();
        assertThat(result.get(1).avgRating()).isNull();
        assertThat(result.get(1).reviewCount()).isZero();
    }
}
