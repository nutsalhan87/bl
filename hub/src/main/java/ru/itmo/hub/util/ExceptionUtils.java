package ru.itmo.hub.util;

import ru.itmo.hub.exception.ServiceException;

public class ExceptionUtils {
    public static void checkPageNumber(int page) {
        if (page < 1) {
            throw ServiceException.illegalPageNumber();
        }
    }
}
