package com.japantravel.recommendation.dto;

import java.util.List;

//  AI 추천 방문 시기 (M4-2). 축제는 개최 월(festivals.month)로 거르고, 여행지는 label 을 프롬프트에 넣어 LLM 이 판단한다
public enum Season {
    SPRING("봄(3~5월)", List.of(3, 4, 5)),
    SUMMER("여름(6~8월)", List.of(6, 7, 8)),
    AUTUMN("가을(9~11월)", List.of(9, 10, 11)),
    WINTER("겨울(12~2월)", List.of(12, 1, 2));

    private final String label;
    private final List<Integer> months;

    Season(String label, List<Integer> months) {
        this.label = label;
        this.months = months;
    }

    public String label() {
        return label;
    }

    public List<Integer> months() {
        return months;
    }
}
