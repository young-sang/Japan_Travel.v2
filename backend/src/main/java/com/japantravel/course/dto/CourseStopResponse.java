package com.japantravel.course.dto;

import com.japantravel.course.entity.CourseStop;
import com.japantravel.destination.entity.Destination;
import com.japantravel.festival.entity.Festival;

//  원본 장소를 평탄화한다 (D-046). prefecture 는 원본 장소의 현 — 코스의 대표 현과 다를 수 있다.
public record CourseStopResponse(
        Integer dayNo,
        Integer seq,
        StopType type,
        Long targetId,
        String name,
        String prefecture,
        Double lat,
        Double lng,
        String imagePath,
        String memo
) {
    public static CourseStopResponse from(CourseStop s) {
        if (s.getDestination() != null) {
            Destination d = s.getDestination();
            return new CourseStopResponse(s.getDayNo(), s.getSeq(), StopType.DESTINATION, d.getId(),
                    d.getName(), d.getPrefecture().getName(), d.getLat(), d.getLng(), d.getImagePath(), s.getMemo());
        }
        Festival f = s.getFestival();
        return new CourseStopResponse(s.getDayNo(), s.getSeq(), StopType.FESTIVAL, f.getId(),
                f.getName(), f.getPrefecture().getName(), f.getLat(), f.getLng(), f.getImagePath(), s.getMemo());
    }
}
