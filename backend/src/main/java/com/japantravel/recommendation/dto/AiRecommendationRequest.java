package com.japantravel.recommendation.dto;

//  AI 추천 페이지 입력 (M4-1). preference = 취향 문장, prefecture = 도도부현 한글 이름 (예: "오이타현")
public record AiRecommendationRequest(String preference, String prefecture) {
}
