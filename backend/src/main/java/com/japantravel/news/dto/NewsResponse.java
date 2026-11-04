package com.japantravel.news.dto;

import com.japantravel.news.entity.News;

import java.time.LocalDateTime;

//  상세 · 작성 · 수정 응답. 작성자 계정이 지워졌으면 authorNickname 은 null
public record NewsResponse(
        Long id,
        String title,
        String body,
        String imagePath,
        String imageCredit,
        String authorNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NewsResponse from(News n) {
        return new NewsResponse(
                n.getId(),
                n.getTitle(),
                n.getBody(),
                n.getImagePath(),
                n.getImageCredit(),
                n.getAuthor() == null ? null : n.getAuthor().getNickname(),
                n.getCreatedAt(),
                n.getUpdatedAt()
        );
    }
}
