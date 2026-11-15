package com.japantravel.recommendation.dto;

//  AI 추천 페이지 입력 (M4-1 · M4-2). preference = 취향 문장, prefecture = 도도부현 한글 이름 (예: "오이타현")
//  kind 생략 = 여행지 + 축제, season 생략 = 상관없음. 잘못된 enum 값은 Jackson 이 읽지 못해 MALFORMED_REQUEST
public record AiRecommendationRequest(String preference, String prefecture, AiPlaceKind kind, Season season) {
}
