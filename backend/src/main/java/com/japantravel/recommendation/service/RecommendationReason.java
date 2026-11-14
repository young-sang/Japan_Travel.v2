package com.japantravel.recommendation.service;

import com.japantravel.recommendation.dto.ActivityKind;

//  선정 이유 문장 (D-100). 근거 장소와 활동 종류로 템플릿을 채운다.
//  예: "즐겨찾기한 '오타루 운하'와 비슷한 곳이에요", "최근 본 '오사카성'과 비슷한 곳이에요"
final class RecommendationReason {

    private RecommendationReason() {
    }

    static String write(ActivityKind kind, String name) {
        String did = switch (kind) {
            case FAVORITE -> "즐겨찾기한";
            case REVIEW -> "리뷰를 남긴";
            case HISTORY -> "최근 본";
        };
        return did + " '" + name + "'" + withOrGwa(name) + " 비슷한 곳이에요";
    }

//  조사 와/과 — 이름의 마지막 한글 글자에 받침이 없으면 "와", 있으면 "과".
//  "히카와 신사 (氷川神社)" 처럼 한글이 아닌 글자로 끝나면 그 앞의 마지막 한글을 본다. 한글이 없으면 "와"
    static String withOrGwa(String name) {
        for (int i = name.length() - 1; i >= 0; i--) {
            char c = name.charAt(i);
            if (c >= '가' && c <= '힣') {
                return (c - '가') % 28 == 0 ? "와" : "과";
            }
        }
        return "와";
    }
}
