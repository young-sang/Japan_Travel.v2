package com.japantravel.course.dto;

import java.util.List;

//  작성 · 수정 공통. 수정은 전체 덮어쓰기라 모양이 같다.
//  필수 값 판정은 CourseService.validate — 빠지면 400 INVALID_COURSE (D-047).
public record CourseRequest(
        String title,
        String description,
        String prefecture,
        String imagePath,
        Boolean isPublic,
        List<CourseStopRequest> stops
) {
}
