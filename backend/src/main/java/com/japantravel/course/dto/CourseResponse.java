package com.japantravel.course.dto;

import com.japantravel.course.entity.Course;
import com.japantravel.course.entity.CourseStop;

import java.time.LocalDateTime;
import java.util.List;

//  상세 · 작성 · 수정 응답 = 요약 + description · updatedAt · stops (D-046).
//  stops 는 (dayNo, seq) 오름차순 — Course.stops 의 @OrderBy 가 정한다.
//  imagePath 가 비었으면 첫 사진 있는 정류장의 사진과 출처를 쓴다 (Course.coverStop).
//  코스가 자기 사진을 가지면 imageCredit · imageSource 는 null.
public record CourseResponse(
        Long id,
        String title,
        String description,
        String prefecture,
        String imagePath,
        String imageCredit,
        String imageSource,
        boolean isPublic,
        Long ownerId,
        String ownerNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CourseStopResponse> stops
) {
    public static CourseResponse from(Course c) {
        CourseStop cover = c.coverStop();
        return new CourseResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getPrefecture().getName(),
                cover == null ? c.getImagePath() : cover.placeImagePath(),
                cover == null ? null : cover.placeImageCredit(),
                cover == null ? null : cover.placeImageSource(),
                c.isPublic(),
                c.getOwner() == null ? null : c.getOwner().getId(),
                c.getOwner() == null ? null : c.getOwner().getNickname(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getStops().stream().map(CourseStopResponse::from).toList()
        );
    }
}
