package ru.itmo.hub.util;

import org.springframework.data.domain.Pageable;

public class ControllerUtils {
    public static Pageable getPageableFromPageNumberAndSize(int pageNumber, int pageSize) {
        return Pageable.ofSize(pageSize).withPage(pageNumber - 1);
    }
}
