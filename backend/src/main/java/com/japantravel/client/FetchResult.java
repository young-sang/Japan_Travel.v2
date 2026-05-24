package com.japantravel.client;

import java.util.Optional;

public record FetchResult<T>(T value, FailureKind kind, String exceptionClass, String message) {

    public enum FailureKind {
        NONE, NOT_FOUND, RATE_LIMITED, TIMEOUT, HTTP_ERROR, NETWORK, PARSE
    }

    public static <T> FetchResult<T> success(T value) {
        return new FetchResult<>(value, FailureKind.NONE, null, null);
    }

    public static <T> FetchResult<T> notFound() {
        return new FetchResult<>(null, FailureKind.NOT_FOUND, null, null);
    }

    public static <T> FetchResult<T> error(FailureKind kind, Throwable t) {
        return new FetchResult<>(null, kind, t.getClass().getSimpleName(), t.getMessage());
    }

    public static <T> FetchResult<T> error(FailureKind kind, String exceptionClass, String message) {
        return new FetchResult<>(null, kind, exceptionClass, message);
    }

    public boolean isSuccess() {
        return kind == FailureKind.NONE;
    }

    public boolean isFailure() {
        return kind != FailureKind.NONE && kind != FailureKind.NOT_FOUND;
    }

    public Optional<T> toOptional() {
        return isSuccess() ? Optional.of(value) : Optional.empty();
    }
}
