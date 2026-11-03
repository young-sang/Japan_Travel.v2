package com.japantravel.festival.dto;

import com.japantravel.festival.entity.Festival;

import java.time.LocalDateTime;

//  목록과 상세가 같이 쓴다. destination 과 같은 규칙.
public record FestivalResponse(
        Long id,
        String name,
        String prefecture,
        Integer month,
        String dateText,
        String description,
        Double lat,
        Double lng,
        String imagePath,
        String imageCredit,
        String imageSource,
        LocalDateTime createdAt,
//      리뷰 집계 (D-057). 평균은 소수 첫째 자리, 리뷰가 없으면 null
        Double avgRating,
        long reviewCount
) {
    public static FestivalResponse from(Festival f) {
        return new FestivalResponse(
                f.getId(),
                f.getName(),
                f.getPrefecture() == null ? null : f.getPrefecture().getName(),
                f.getMonth(),
                f.getDateText(),
                f.getDescription(),
                f.getLat(),
                f.getLng(),
                f.getImagePath(),
                f.getImageCredit(),
                f.getImageSource(),
                f.getCreatedAt(),
                round1(f.getAvgRating()),
                f.getReviewCount()
        );
    }

    private static Double round1(Double avg) {
        return avg == null ? null : Math.round(avg * 10) / 10.0;
    }
}
