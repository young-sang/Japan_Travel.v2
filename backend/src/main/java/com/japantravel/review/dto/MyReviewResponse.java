package com.japantravel.review.dto;

import com.japantravel.review.entity.ReviewDestination;
import com.japantravel.review.entity.ReviewFestival;

import java.time.LocalDateTime;

//  내 리뷰 (D-055). 작성자는 나이므로 싣지 않고, 대신 대상의 종류 · id · 이름을 싣는다.
//  type + targetId 로 프론트가 상세 링크와 수정 · 삭제 경로(/api/{type}s/{targetId}/reviews/{id})를 만든다.
public record MyReviewResponse(
        Long id,
        ReviewTargetType type,
        Long targetId,
        String targetName,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MyReviewResponse from(ReviewDestination r) {
        return new MyReviewResponse(r.getId(), ReviewTargetType.DESTINATION,
                r.getDestination().getId(), r.getDestination().getName(),
                r.getRating(), r.getComment(), r.getCreatedAt(), r.getUpdatedAt());
    }

    public static MyReviewResponse from(ReviewFestival r) {
        return new MyReviewResponse(r.getId(), ReviewTargetType.FESTIVAL,
                r.getFestival().getId(), r.getFestival().getName(),
                r.getRating(), r.getComment(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
