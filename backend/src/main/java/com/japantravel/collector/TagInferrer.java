package com.japantravel.collector;

import org.springframework.stereotype.Component;

import java.util.*;

/** Wikipedia 페이지 제목·설명에서 graduate project 디자인이 사용하는 태그 셋을 추론. */
@Component
public class TagInferrer {

    /** destination 태그 후보. */
    private static final Map<String, List<String>> DEST_KEYWORDS = new LinkedHashMap<>();
    static {
        DEST_KEYWORDS.put("역사",     List.of("절", "신사", "사찰", "성", "유적", "고분", "박물관", "사적", "古"));
        DEST_KEYWORDS.put("자연",     List.of("산", "강", "호수", "폭포", "공원", "숲", "섬", "협곡", "동굴"));
        DEST_KEYWORDS.put("바다",     List.of("해변", "해안", "해수욕", "바다", "비치", "灣"));
        DEST_KEYWORDS.put("온천",     List.of("온천", "료칸"));
        DEST_KEYWORDS.put("도시",     List.of("거리", "번화가", "쇼핑", "긴자", "신주쿠", "시부야", "도시"));
        DEST_KEYWORDS.put("쇼핑",     List.of("시장", "백화점", "쇼핑", "아울렛"));
        DEST_KEYWORDS.put("맛집",     List.of("시장", "라멘", "스시", "요리", "음식"));
        DEST_KEYWORDS.put("랜드마크", List.of("타워", "전망대", "빌딩", "스카이트리", "다리"));
        DEST_KEYWORDS.put("등산",     List.of("산", "트레킹", "등산"));
        DEST_KEYWORDS.put("축제",     List.of("마츠리", "축제", "祭"));
    }

    public List<String> inferDestinationTags(String name, String extract) {
        String text = (name == null ? "" : name) + " " + (extract == null ? "" : extract);
        List<String> out = new ArrayList<>();
        for (var e : DEST_KEYWORDS.entrySet()) {
            for (String kw : e.getValue()) {
                if (text.contains(kw)) { out.add(e.getKey()); break; }
            }
        }
        if (out.isEmpty()) out.add("관광지");
        return out;
    }

    private static final Map<String, Integer> MONTH_KEYWORDS = Map.ofEntries(
            Map.entry("1월", 1), Map.entry("2월", 2), Map.entry("3월", 3),
            Map.entry("4월", 4), Map.entry("5월", 5), Map.entry("6월", 6),
            Map.entry("7월", 7), Map.entry("8월", 8), Map.entry("9월", 9),
            Map.entry("10월", 10), Map.entry("11월", 11), Map.entry("12월", 12)
    );

    public Integer inferMonth(String extract) {
        if (extract == null) return null;
        for (var e : MONTH_KEYWORDS.entrySet()) {
            if (extract.contains(e.getKey())) return e.getValue();
        }
        return null;
    }
}
