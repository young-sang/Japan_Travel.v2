package com.japantravel.recommendation.dto;

//  사용자 활동 종류. 선언 순서가 강한 순이다 — 한 장소에 여러 활동이면 앞의 것으로 이유를 쓴다 (D-100)
public enum ActivityKind {
    FAVORITE,
    REVIEW,
    HISTORY
}
