package com.japantravel.course.dto;

import com.japantravel.course.entity.Course;

import java.time.LocalDateTime;
import java.util.List;

//  상세 · 작성 · 수정 응답 = 요약 + description · updatedAt · stops (D-046).
//  stops 는 (dayNo, seq) 오름차순 — Course.stops 의 @OrderBy 가 정한다.
public record CourseResponse(
        Long id,
        String title,
        String description,
        String prefecture,
        String imagePath,
        boolean isPublic,
        Long ownerId,
        String ownerNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CourseStopResponse> stops
) {
    public static CourseResponse from(Course c) {
        return new CourseResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getPrefecture().getName(),
                c.getImagePath(),
                c.isPublic(),
                c.getOwner() == null ? null : c.getOwner().getId(),
                c.getOwner() == null ? null : c.getOwner().getNickname(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getStops().stream().map(CourseStopResponse::from).toList()
        );
    }
}
