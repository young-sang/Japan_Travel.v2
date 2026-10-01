package com.japantravel.history.dto;

import com.japantravel.destination.entity.Destination;
import com.japantravel.festival.entity.Festival;
import com.japantravel.history.entity.HistoryDestination;
import com.japantravel.history.entity.HistoryFestival;

import java.time.LocalDateTime;

//  여행지·축제를 한 배열에 섞어 시간순으로 내보내므로 원본 장소를 평탄화한다 (D-052).
//  카드에 필요한 것까지 실어서 프론트가 항목마다 상세를 다시 부르지 않게 한다.
public record HistoryItemResponse(
        HistoryTargetType type,
        Long targetId,
        String name,
        String prefecture,
        String imagePath,
        LocalDateTime visitedAt
) {
    public static HistoryItemResponse from(HistoryDestination h) {
        Destination d = h.getDestination();
        return new HistoryItemResponse(HistoryTargetType.DESTINATION, d.getId(),
                d.getName(), d.getPrefecture().getName(), d.getImagePath(), h.getVisitedAt());
    }

    public static HistoryItemResponse from(HistoryFestival h) {
        Festival f = h.getFestival();
        return new HistoryItemResponse(HistoryTargetType.FESTIVAL, f.getId(),
                f.getName(), f.getPrefecture().getName(), f.getImagePath(), h.getVisitedAt());
    }
}
