package com.japantravel.course.dto;

//  seq 는 받지 않는다 — 서버가 같은 dayNo 안에서 배열 순서대로 매긴다 (D-046).
public record CourseStopRequest(
        Integer dayNo,
        StopType type,
        Long targetId,
        String memo
) {
}
