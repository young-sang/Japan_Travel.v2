package com.japantravel.review;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.review.dto.MyReviewResponse;
import com.japantravel.review.dto.ReviewRequest;
import com.japantravel.review.dto.ReviewResponse;
import com.japantravel.review.dto.ReviewTargetType;
import com.japantravel.review.entity.ReviewDestination;
import com.japantravel.review.entity.ReviewFestival;
import com.japantravel.review.repository.ReviewDestinationRepository;
import com.japantravel.review.repository.ReviewFestivalRepository;
import com.japantravel.review.service.ReviewService;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.japantravel.support.Fixtures.destination;
import static com.japantravel.support.Fixtures.festival;
import static com.japantravel.support.Fixtures.user;
import static com.japantravel.support.Fixtures.withId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewDestinationRepository reviewDestinationRepository;
    @Mock ReviewFestivalRepository reviewFestivalRepository;
    @Mock UserRepository userRepository;
    @Mock DestinationRepository destinationRepository;
    @Mock FestivalRepository festivalRepository;

    @InjectMocks ReviewService reviewService;

    private static void assertCode(Runnable call, ErrorCode code) {
        assertThatThrownBy(call::run).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode()).isEqualTo(code);
    }

    private ReviewDestination review(long id, User author, Destination destination, int rating) {
        return withId(new ReviewDestination(author, destination, rating, null), id);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, 6})
    void 별점이_범위_밖이면_아무것도_읽기_전에_INVALID_RATING(Integer rating) {
        assertCode(() -> reviewService.createForDestination(1L, 10L, new ReviewRequest(rating, null)),
                ErrorCode.INVALID_RATING);
        assertCode(() -> reviewService.updateForDestination(1L, 10L, 100L, new ReviewRequest(rating, null)),
                ErrorCode.INVALID_RATING);

        verifyNoInteractions(destinationRepository, reviewDestinationRepository, userRepository);
    }

    @Test
    void 작성하면_작성자와_대상을_묶어_저장한다() {
        User author = user(1L);
        Destination destination = destination(10L);
        when(destinationRepository.findById(10L)).thenReturn(Optional.of(destination));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(reviewDestinationRepository.save(any())).thenAnswer(inv -> withId(inv.getArgument(0), 100L));

        ReviewResponse res = reviewService.createForDestination(1L, 10L, new ReviewRequest(4, "좋다"));

        assertThat(res.id()).isEqualTo(100L);
        assertThat(res.userId()).isEqualTo(1L);
        assertThat(res.rating()).isEqualTo(4);
        assertThat(res.updatedAt()).isNull();
    }

    @Test
    void 사용자가_지워졌으면_UNAUTHORIZED() {
        Destination d10 = destination(10L);
        when(destinationRepository.findById(10L)).thenReturn(Optional.of(d10));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertCode(() -> reviewService.createForDestination(1L, 10L, new ReviewRequest(3, null)),
                ErrorCode.UNAUTHORIZED);
        verify(reviewDestinationRepository, never()).save(any());
    }

    @Test
    void 없는_대상이면_목록도_404() {
        when(destinationRepository.existsById(10L)).thenReturn(false);

        assertCode(() -> reviewService.findByDestination(10L), ErrorCode.DESTINATION_NOT_FOUND);
    }

    @Test
    void 리뷰가_없으면_작성자를_보기_전에_REVIEW_NOT_FOUND() {
        when(reviewDestinationRepository.findByIdAndDestinationId(100L, 10L)).thenReturn(Optional.empty());

        assertCode(() -> reviewService.deleteForDestination(2L, 10L, 100L), ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    void 작성자가_아니면_FORBIDDEN_이고_바뀌지_않는다() {
        ReviewDestination review = review(100L, user(1L), destination(10L), 3);
        when(reviewDestinationRepository.findByIdAndDestinationId(100L, 10L)).thenReturn(Optional.of(review));

        assertCode(() -> reviewService.updateForDestination(2L, 10L, 100L, new ReviewRequest(5, "x")),
                ErrorCode.FORBIDDEN);
        assertCode(() -> reviewService.deleteForDestination(2L, 10L, 100L), ErrorCode.FORBIDDEN);

        assertThat(review.getRating()).isEqualTo(3);
        verify(reviewDestinationRepository, never()).delete(any());
    }

    @Test
    void 작성자는_두_필드를_모두_덮어쓴다() {
        ReviewDestination review = withId(new ReviewDestination(user(1L), destination(10L), 3, "처음"), 100L);
        when(reviewDestinationRepository.findByIdAndDestinationId(100L, 10L)).thenReturn(Optional.of(review));

        ReviewResponse res = reviewService.updateForDestination(1L, 10L, 100L, new ReviewRequest(5, null));

        assertThat(res.rating()).isEqualTo(5);
        assertThat(res.comment()).isNull();
        assertThat(res.updatedAt()).isNotNull();
    }

    @Test
    void 내_리뷰는_두_테이블을_합쳐_작성_시각_내림차순() {
        User me = user(1L);
        ReviewDestination old = review(1L, me, destination(10L), 4);
        ReviewDestination newest = review(2L, me, destination(11L), 5);
        ReviewFestival middle = withId(new ReviewFestival(me, festival(20L), 3, null), 3L);
        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(old, "createdAt", now.minusHours(3));
        ReflectionTestUtils.setField(middle, "createdAt", now.minusHours(2));
        ReflectionTestUtils.setField(newest, "createdAt", now.minusHours(1));
        when(reviewDestinationRepository.findByUserIdOrderByIdDesc(1L)).thenReturn(List.of(newest, old));
        when(reviewFestivalRepository.findByUserIdOrderByIdDesc(1L)).thenReturn(List.of(middle));

        List<MyReviewResponse> mine = reviewService.findMine(1L);

        assertThat(mine).extracting(MyReviewResponse::id).containsExactly(2L, 3L, 1L);
        assertThat(mine.get(1).type()).isEqualTo(ReviewTargetType.FESTIVAL);
        assertThat(mine.get(1).targetName()).isEqualTo("축제20");
    }
}
