package com.japantravel.course.dto;

import com.japantravel.course.entity.Course;

// API 계약이다. 엔티티를 컨트롤러 밖으로 내보내지 않기 위해 존재한다.
// 상세 · 작성 · 수정 응답. 목록은 요약(CourseSummaryResponse)을 따로 둔다 (D-046).
public record CourseResponse(
        Long id
        // schema.sql 과 승인된 설계를 보고 채운다
) {
    public static CourseResponse from(Course entity) {
        return new CourseResponse(entity.getId());
    }
}
