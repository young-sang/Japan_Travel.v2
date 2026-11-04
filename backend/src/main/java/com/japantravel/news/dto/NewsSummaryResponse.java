package com.japantravel.news.dto;

import com.japantravel.news.entity.News;

import java.time.LocalDateTime;

//  목록 한 칸. 본문 대신 앞 100자 미리보기를 싣는다 (D-079)
public record NewsSummaryResponse(
        Long id,
        String title,
        String excerpt,
        String imagePath,
        LocalDateTime createdAt
) {
    private static final int EXCERPT_LENGTH = 100;

    public static NewsSummaryResponse from(News n) {
        return new NewsSummaryResponse(n.getId(), n.getTitle(), excerpt(n.getBody()), n.getImagePath(), n.getCreatedAt());
    }

//  줄바꿈은 공백으로 펴고, 넘치면 자른 뒤 "…" 를 붙인다
    private static String excerpt(String body) {
        String flat = body.replaceAll("\\s+", " ").strip();
        return flat.length() <= EXCERPT_LENGTH ? flat : flat.substring(0, EXCERPT_LENGTH) + "…";
    }
}
