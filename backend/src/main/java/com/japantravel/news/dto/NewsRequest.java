package com.japantravel.news.dto;

//  작성 · 수정 본문. 수정은 네 필드를 모두 덮어쓴다 (D-079)
public record NewsRequest(
        String title,
        String body,
        String imagePath,
        String imageCredit
) {
}
