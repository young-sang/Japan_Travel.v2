package com.japantravel.course.dto;

import com.japantravel.course.entity.Course;
import com.japantravel.course.entity.CourseStop;

import java.time.LocalDateTime;

//  공개 목록 · 내 코스 목록. 정류장을 싣지 않는다 (D-046).
//  ownerId 는 프론트가 /api/auth/me 의 id 와 비교해 내 코스에만 수정 버튼을 보이기 위해 싣는다.
//  기본 제공 코스는 ownerId · ownerNickname 이 null.
//  imagePath 가 비었으면 첫 사진 있는 정류장의 사진을 쓴다 (Course.coverStop).
public record CourseSummaryResponse(
        Long id,
        String title,
        String prefecture,
        String imagePath,
        boolean isPublic,
        Long ownerId,
        String ownerNickname,
        LocalDateTime createdAt
) {
    public static CourseSummaryResponse from(Course c) {
        CourseStop cover = c.coverStop();
        return new CourseSummaryResponse(
                c.getId(),
                c.getTitle(),
                c.getPrefecture().getName(),
                cover == null ? c.getImagePath() : cover.placeImagePath(),
                c.isPublic(),
                c.getOwner() == null ? null : c.getOwner().getId(),
                c.getOwner() == null ? null : c.getOwner().getNickname(),
                c.getCreatedAt()
        );
    }
}
