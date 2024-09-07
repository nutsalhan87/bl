package ru.itmo.hub.util;

import org.springframework.lang.NonNull;

public class FormatUtils {
    public static String normalizeTag(@NonNull String value) {
        return value.replaceAll("\\s+", "_").toLowerCase();
    }
}
