package com.japantravel.search.dto;

import com.japantravel.course.dto.CourseSummaryResponse;
import com.japantravel.destination.dto.DestinationResponse;
import com.japantravel.festival.dto.FestivalResponse;

import java.util.List;

//  대상별로 배열 하나씩. 요소는 각 도메인의 목록 DTO 를 재사용한다 (D-053).
public record SearchResponse(
        List<DestinationResponse> destinations,
        List<FestivalResponse> festivals,
        List<CourseSummaryResponse> courses
) {
    public static SearchResponse empty() {
        return new SearchResponse(List.of(), List.of(), List.of());
    }
}
