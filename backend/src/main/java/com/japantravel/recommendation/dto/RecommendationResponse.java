package com.japantravel.recommendation.dto;

//  추천 카드 한 장. 여행지 · 축제를 한 배열에 섞으므로 HistoryItemResponse 처럼 평탄화한다 (D-098)
public record RecommendationResponse(
        RecommendationTargetType type,
        Long targetId,
        String name,
        String prefecture,
        String imagePath,
        double similarity
) {
}
