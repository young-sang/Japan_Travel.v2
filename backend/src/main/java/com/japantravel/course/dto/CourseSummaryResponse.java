package com.japantravel.course.dto;

import com.japantravel.course.entity.Course;

import java.time.LocalDateTime;

//  공개 목록 · 내 코스 목록. 정류장을 싣지 않는다 (D-046).
//  ownerId 는 프론트가 /api/auth/me 의 id 와 비교해 내 코스에만 수정 버튼을 보이기 위해 싣는다.
//  기본 제공 코스는 ownerId · ownerNickname 이 null.
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
        return new CourseSummaryResponse(
                c.getId(),
                c.getTitle(),
                c.getPrefecture().getName(),
                c.getImagePath(),
                c.isPublic(),
                c.getOwner() == null ? null : c.getOwner().getId(),
                c.getOwner() == null ? null : c.getOwner().getNickname(),
                c.getCreatedAt()
        );
    }
}
