package com.japantravel.destination.dto;

//  Top25 한 칸 (D-076). 리뷰 수 · 평균 평점은 destination 안에 있다.
public record DestinationRankingResponse(
        int rank,
        long score,
        long viewerCount,
        long favoriteCount,
        DestinationResponse destination
) {
}
