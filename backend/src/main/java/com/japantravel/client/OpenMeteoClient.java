package com.japantravel.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Component
public class OpenMeteoClient {

    private final WebClient web;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpenMeteoClient(WebClient.Builder builder,
                           @Value("${external.open-meteo.base-url}") String baseUrl) {
        this.web = builder.baseUrl(baseUrl).build();
    }

    @Cacheable(cacheNames = "weather", key = "#lat + ',' + #lng")
    public Optional<Current> current(double lat, double lng) {
        String path = "/v1/forecast?latitude=" + lat + "&longitude=" + lng + "&current=temperature_2m,weather_code";
        try {
            String body = web.get().uri(path).retrieve().bodyToMono(String.class).block();
            JsonNode root = mapper.readTree(body);
            JsonNode cur = root.path("current");
            return Optional.of(new Current(cur.path("temperature_2m").asDouble(), cur.path("weather_code").asInt()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public record Current(double temperature, int weatherCode) {}
}
