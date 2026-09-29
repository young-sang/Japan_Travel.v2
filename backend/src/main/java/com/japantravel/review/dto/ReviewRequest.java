package com.japantravel.review.dto;

//  작성 · 수정 공용. 수정은 두 필드를 모두 덮어쓴다 (부분 수정 아님).
//  comment 는 선택 — 별점만 남길 수 있다.
public record ReviewRequest(
        Integer rating,
        String comment
) {
}
