package com.japantravel.favorite.dto;

import com.japantravel.destination.dto.DestinationResponse;
import com.japantravel.festival.dto.FestivalResponse;

import java.util.List;

//  대상별 테이블을 그대로 두 배열로 내보낸다. 요소는 각 도메인의 DTO 를 재사용한다.
public record FavoriteListResponse(
        List<DestinationResponse> destinations,
        List<FestivalResponse> festivals
) {
}
