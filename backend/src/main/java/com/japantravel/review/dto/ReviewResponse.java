package com.japantravel.review.dto;

import com.japantravel.review.entity.ReviewDestination;
import com.japantravel.review.entity.ReviewFestival;

import java.time.LocalDateTime;

//  여행지 · 축제 리뷰가 같은 모양으로 나간다.
//  userId 는 프론트가 /api/auth/me 의 id 와 비교해 "내 리뷰" 에만 수정·삭제 버튼을 보이기 위해 싣는다.
public record ReviewResponse(
        Long id,
        Long userId,
        String nickname,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse from(ReviewDestination r) {
        return new ReviewResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getNickname(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    public static ReviewResponse from(ReviewFestival r) {
        return new ReviewResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getNickname(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
