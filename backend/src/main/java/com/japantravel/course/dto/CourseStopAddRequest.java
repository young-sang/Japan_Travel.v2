package com.japantravel.course.dto;

//  내 코스에 정류장 하나 더하기 (D-094). 일차 · 순서는 받지 않는다 — 서버가 마지막 일차의 끝에 붙인다.
public record CourseStopAddRequest(
        StopType type,
        Long targetId,
        String memo
) {
}
