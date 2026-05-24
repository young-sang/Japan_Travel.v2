package com.japantravel.dto;

import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record Destination(
            Long id, String name, String prefecture, List<String> tags,
            Double lat, Double lng, String imagePath, String description,
            String wikiTitle, String lastRefreshedAt
    ) {}

    public record Festival(
            Long id, String name, String prefecture, Integer month, String dateText,
            String imagePath, String description, String wikiTitle,
            Double lat, Double lng, String lastRefreshedAt
    ) {}

    public record CourseStop(String time, String title, String desc, Double lat, Double lng) {}

    public record Course(
            Long id, String title, String prefecture, List<String> tags, String duration,
            String imagePath, Double centerLat, Double centerLng,
            List<CourseStop> timeline, boolean isUserCreated, Long ownerUserId,
            String status, String createdAt
    ) {}

    public record Favorite(String targetType, Long targetId, String createdAt) {}

    public record Review(
            Long id, Long userId, String targetType, Long targetId,
            Integer rating, String comment, String createdAt
    ) {}

    public record HistoryItem(String targetType, Long targetId, String visitedAt) {}

    public record CollectorRunRequest(String type, String prefecture) {}

    public record CollectorRunStatus(
            Long id, String type, String prefecture, String source, String status,
            Integer itemsAdded, Integer itemsUpdated, Integer itemsFailed,
            String startedAt, String finishedAt
    ) {}

    public record FavoriteCreateRequest(String targetType, Long targetId) {}

    public record ReviewCreateRequest(String targetType, Long targetId, Integer rating, String comment) {}
}
