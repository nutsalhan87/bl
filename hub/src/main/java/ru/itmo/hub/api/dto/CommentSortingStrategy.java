package ru.itmo.hub.api.dto;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;

public enum CommentSortingStrategy {
    NEWER("NEWER"),
    OLDER("OLDER");

    private final String value;

    CommentSortingStrategy(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static class StringToEnumConverter implements Converter<String, CommentSortingStrategy> {
        @Override
        public CommentSortingStrategy convert(@NonNull String source) {
            try {
                return CommentSortingStrategy.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return NEWER;
            }
        }
    }
}
