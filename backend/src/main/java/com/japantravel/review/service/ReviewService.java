package com.japantravel.review.service;

import com.japantravel.common.error.ApiException;
import com.japantravel.common.error.ErrorCode;
import com.japantravel.destination.entity.Destination;
import com.japantravel.destination.repository.DestinationRepository;
import com.japantravel.festival.entity.Festival;
import com.japantravel.festival.repository.FestivalRepository;
import com.japantravel.review.dto.ReviewRequest;
import com.japantravel.review.dto.ReviewResponse;
import com.japantravel.review.entity.ReviewDestination;
import com.japantravel.review.entity.ReviewFestival;
import com.japantravel.review.repository.ReviewDestinationRepository;
import com.japantravel.review.repository.ReviewFestivalRepository;
import com.japantravel.user.entity.User;
import com.japantravel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//  수정 · 삭제의 판정 순서: 별점 → 리뷰 존재(경로의 대상에 딸렸는지 포함) → 작성자.
//  없는 리뷰에 403 을 주지 않는다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewDestinationRepository reviewDestinationRepository;
    private final ReviewFestivalRepository reviewFestivalRepository;
    private final UserRepository userRepository;
//  대상 도메인은 존재 확인과 참조용으로만 읽는다
    private final DestinationRepository destinationRepository;
    private final FestivalRepository festivalRepository;

//  ---- destination ----

    public List<ReviewResponse> findByDestination(Long destinationId) {
//      실재하는 여행지인데 0건이면 빈 목록, 없는 여행지면 404
        if (!destinationRepository.existsById(destinationId)) {
            throw new ApiException(ErrorCode.DESTINATION_NOT_FOUND);
        }
        return reviewDestinationRepository.findByDestinationIdOrderByIdDesc(destinationId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public ReviewResponse createForDestination(Long userId, Long destinationId, ReviewRequest req) {
        validateRating(req.rating());
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ApiException(ErrorCode.DESTINATION_NOT_FOUND));
        ReviewDestination review = reviewDestinationRepository.save(
                new ReviewDestination(findUser(userId), destination, req.rating(), req.comment()));
        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse updateForDestination(Long userId, Long destinationId, Long reviewId, ReviewRequest req) {
        validateRating(req.rating());
        ReviewDestination review = reviewDestinationRepository.findByIdAndDestinationId(reviewId, destinationId)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
        checkOwner(review.getUser(), userId);
        review.update(req.rating(), req.comment());
        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteForDestination(Long userId, Long destinationId, Long reviewId) {
        ReviewDestination review = reviewDestinationRepository.findByIdAndDestinationId(reviewId, destinationId)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
        checkOwner(review.getUser(), userId);
        reviewDestinationRepository.delete(review);
    }

//  ---- festival ----

    public List<ReviewResponse> findByFestival(Long festivalId) {
        if (!festivalRepository.existsById(festivalId)) {
            throw new ApiException(ErrorCode.FESTIVAL_NOT_FOUND);
        }
        return reviewFestivalRepository.findByFestivalIdOrderByIdDesc(festivalId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public ReviewResponse createForFestival(Long userId, Long festivalId, ReviewRequest req) {
        validateRating(req.rating());
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new ApiException(ErrorCode.FESTIVAL_NOT_FOUND));
        ReviewFestival review = reviewFestivalRepository.save(
                new ReviewFestival(findUser(userId), festival, req.rating(), req.comment()));
        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse updateForFestival(Long userId, Long festivalId, Long reviewId, ReviewRequest req) {
        validateRating(req.rating());
        ReviewFestival review = reviewFestivalRepository.findByIdAndFestivalId(reviewId, festivalId)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
        checkOwner(review.getUser(), userId);
        review.update(req.rating(), req.comment());
        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteForFestival(Long userId, Long festivalId, Long reviewId) {
        ReviewFestival review = reviewFestivalRepository.findByIdAndFestivalId(reviewId, festivalId)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));
        checkOwner(review.getUser(), userId);
        reviewFestivalRepository.delete(review);
    }

//  ---- 공통 ----

//  DB 의 CHECK 에만 맡기면 위반이 500 으로 나간다. 먼저 400 으로 거른다.
    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ApiException(ErrorCode.INVALID_RATING);
        }
    }

    private void checkOwner(User author, Long userId) {
        if (!author.getId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
    }

//  토큰은 유효한데 사용자가 지워졌으면 401 — AuthService.me 와 같은 규칙.
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }
}
