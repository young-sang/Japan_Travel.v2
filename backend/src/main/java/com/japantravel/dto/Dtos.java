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
            String startedAt, String finishedAt,
            Long bulkRunId, String lastHeartbeatAt, String stage
    ) {}

    public record BulkRun(
            Long id, String startedAt, String finishedAt, String status,
            Integer totalTasks, Integer tasksSuccess, Integer tasksPartial,
            Integer tasksFailed, Integer tasksAborted, String notes
    ) {}

    public record FavoriteCreateRequest(String targetType, Long targetId) {}

    public record ReviewCreateRequest(String targetType, Long targetId, Integer rating, String comment) {}

    public record User(Long id, String username, String nickname, String role, String createdAt) {}

    public record SignupRequest(String username, String password, String nickname) {}

    public record LoginRequest(String username, String password) {}

    public record BulkRunSummary(
            long id, String startedAt, String finishedAt, String status,
            int totalTasks, int tasksSuccess, int tasksPartial, int tasksFailed, int tasksAborted
    ) {}

    public record FailureEntry(String title, String stage, String exceptionClass, String message) {}

    public record CollectorRunDetail(
            long id, String type, String prefecture, String status,
            int itemsAdded, int itemsUpdated, int itemsFailed,
            String stage, String lastHeartbeatAt,
            java.util.List<FailureEntry> failures, String abortReason
    ) {}

    public record BulkRunDetail(BulkRunSummary summary, java.util.List<CollectorRunDetail> children) {}

    public record Post(
            Long id, Long userId, String userName,
            String title, String body,
            String createdAt, String updatedAt, int commentCount
    ) {}

    public record PostCreate(String title, String body) {}

    public record PostComment(
            Long id, Long postId, Long userId, String userName,
            String body, String createdAt
    ) {}

    public record CommentCreate(String body) {}

    public record PostPage(List<Post> items, int total, int page, int size) {}
}
