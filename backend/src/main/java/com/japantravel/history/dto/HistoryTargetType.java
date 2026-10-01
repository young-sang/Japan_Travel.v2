package com.japantravel.history.dto;

//  기록이 가리키는 대상의 종류. 응답에만 쓴다 — 기록 요청은 경로가 대상을 정한다 (D-052).
public enum HistoryTargetType {
    DESTINATION,
    FESTIVAL
}
