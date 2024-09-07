package ru.itmo.hub.util;

import ru.itmo.hub.api.dto.TagDto;
import ru.itmo.hub.exception.ServiceException;

public class ValidationUtils {
    public static void validateTagDto(TagDto tagDto) {
        if (tagDto == null || tagDto.getValue() == null) {
            throw new ServiceException("Tag must not be null");
        }
    }
}
