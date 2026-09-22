package com.japantravel.festival.dto;

import com.japantravel.festival.entity.Festival;

import java.time.LocalDateTime;

//  목록과 상세가 같이 쓴다. destination 과 같은 규칙.
public record FestivalResponse(
        Long id,
        String name,
        String prefecture,
        Integer month,
        String dateText,
        String description,
        Double lat,
        Double lng,
        String imagePath,
        LocalDateTime createdAt
) {
    public static FestivalResponse from(Festival f) {
        return new FestivalResponse(
                f.getId(),
                f.getName(),
                f.getPrefecture() == null ? null : f.getPrefecture().getName(),
                f.getMonth(),
                f.getDateText(),
                f.getDescription(),
                f.getLat(),
                f.getLng(),
                f.getImagePath(),
                f.getCreatedAt()
        );
    }
}
