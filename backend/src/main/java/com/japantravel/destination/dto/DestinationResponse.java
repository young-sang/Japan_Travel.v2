package com.japantravel.destination.dto;

import com.japantravel.destination.entity.Destination;

import java.time.LocalDateTime;

//  목록과 상세가 같이 쓴다. 지도 때문에 목록에도 좌표가 필요해 나눌 실익이 없다.
public record DestinationResponse(
        Long id,
        String name,
        String prefecture,
        String description,
        Double lat,
        Double lng,
        String imagePath,
        LocalDateTime createdAt,
//      리뷰 집계 (D-057). 평균은 소수 첫째 자리, 리뷰가 없으면 null
        Double avgRating,
        long reviewCount
) {
    public static DestinationResponse from(Destination d) {
        return new DestinationResponse(
                d.getId(),
                d.getName(),
//              현은 이름 문자열로 평탄화한다 — 상세의 값을 그대로 목록 필터로 되먹일 수 있다
                d.getPrefecture() == null ? null : d.getPrefecture().getName(),
                d.getDescription(),
                d.getLat(),
                d.getLng(),
                d.getImagePath(),
                d.getCreatedAt(),
                round1(d.getAvgRating()),
                d.getReviewCount()
        );
    }

    private static Double round1(Double avg) {
        return avg == null ? null : Math.round(avg * 10) / 10.0;
    }
}
