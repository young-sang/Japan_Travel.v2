package com.japantravel.recommendation.dto;

//  AI 추천 후보 종류 (M4-2). ALL 이면 여행지 · 축제를 함께 넘기고 섞는 방식은 LLM 에 맡긴다
public enum AiPlaceKind {
    ALL, DESTINATION, FESTIVAL;

    public boolean includesDestinations() {
        return this != FESTIVAL;
    }

    public boolean includesFestivals() {
        return this != DESTINATION;
    }
}
