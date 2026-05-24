package com.japantravel.controller;

import com.japantravel.client.FrankfurterClient;
import com.japantravel.client.OpenMeteoClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProxyController {

    private final OpenMeteoClient meteo;
    private final FrankfurterClient fx;

    public ProxyController(OpenMeteoClient meteo, FrankfurterClient fx) {
        this.meteo = meteo; this.fx = fx;
    }

    /** 간단한 도도부현 중심 좌표 매핑 (대표 도시 기준). */
    private static final Map<String, double[]> PREF_CENTER = Map.ofEntries(
            Map.entry("도쿄도", new double[]{35.6762, 139.6503}),
            Map.entry("교토부", new double[]{35.0116, 135.7681}),
            Map.entry("오사카부", new double[]{34.6937, 135.5023}),
            Map.entry("홋카이도", new double[]{43.0642, 141.3469}),
            Map.entry("오키나와현", new double[]{26.2124, 127.6809}),
            Map.entry("후쿠오카현", new double[]{33.5904, 130.4017}),
            Map.entry("아오모리현", new double[]{40.8244, 140.7400}),
            Map.entry("나라현", new double[]{34.6851, 135.8048})
    );

    @GetMapping("/weather")
    public Map<String, Object> weather(@RequestParam(defaultValue = "도쿄도") String prefecture) {
        double[] c = PREF_CENTER.getOrDefault(prefecture, PREF_CENTER.get("도쿄도"));
        return meteo.current(c[0], c[1])
                .<Map<String, Object>>map(cur -> Map.of(
                        "prefecture", prefecture,
                        "temperature", cur.temperature(),
                        "weatherCode", cur.weatherCode()))
                .orElse(Map.of("prefecture", prefecture, "error", "weather unavailable"));
    }

    @GetMapping("/fx")
    public Map<String, Object> fx() {
        return fx.jpyToKrwPer100()
                .<Map<String, Object>>map(v -> Map.of("base", "JPY", "amount", 100, "target", "KRW", "value", v))
                .orElse(Map.of("error", "fx unavailable"));
    }
}
