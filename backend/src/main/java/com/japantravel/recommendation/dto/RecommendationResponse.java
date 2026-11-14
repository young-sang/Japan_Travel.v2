package com.japantravel.recommendation.dto;

//  추천 카드 한 장. 여행지 · 축제를 한 배열에 섞으므로 HistoryItemResponse 처럼 평탄화한다 (D-098)
//  basis 는 이 장소를 고른 근거가 된 내 활동 장소 (D-100). 이유 문장은 API 가 만들지 않는다 (D-101)
public record RecommendationResponse(
        RecommendationTargetType type,
        Long targetId,
        String name,
        String prefecture,
        String imagePath,
        double similarity,
        Basis basis
) {
    public record Basis(RecommendationTargetType type, Long targetId, String name, ActivityKind activity) {
    }
}
