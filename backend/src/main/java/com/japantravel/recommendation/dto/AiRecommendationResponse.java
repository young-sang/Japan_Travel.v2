package com.japantravel.recommendation.dto;

//  AI 추천 카드 한 장 (M4-1). 메인 추천 카드(RecommendationResponse)와 같은 모양에 reason 을 더한다.
//  M4-1 은 여행지만 — type 은 항상 DESTINATION. 리뷰가 없으면 avgRating 은 null
public record AiRecommendationResponse(
        RecommendationTargetType type,
        Long targetId,
        String name,
        String prefecture,
        String imagePath,
        Double avgRating,
        long reviewCount,
        String reason
) {
}
