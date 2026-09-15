package com.japantravel.destination.dtos;

import java.util.List;

public class DestinationDtos {
    private DestinationDtos(){}

    public record Destination(
            Long id, String name, String prefecture,
            List<String> tags, Double lat, Double lng,
            String imagePath, String description,
            String wikiTitle, String lastRefreshedAt
    ){}
}
