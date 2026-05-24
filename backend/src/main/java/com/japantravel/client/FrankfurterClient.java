package com.japantravel.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Component
public class FrankfurterClient {

    private final WebClient web;
    private final ObjectMapper mapper = new ObjectMapper();

    public FrankfurterClient(WebClient.Builder builder,
                             @Value("${external.frankfurter.base-url}") String baseUrl) {
        this.web = builder.baseUrl(baseUrl).build();
    }

    /** 100 JPY 당 KRW 환율. */
    @Cacheable(cacheNames = "fx", key = "'jpy_krw'")
    public Optional<Double> jpyToKrwPer100() {
        try {
            String body = web.get().uri("/latest?from=JPY&to=KRW&amount=100").retrieve().bodyToMono(String.class).block();
            JsonNode root = mapper.readTree(body);
            return Optional.of(root.path("rates").path("KRW").asDouble());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
