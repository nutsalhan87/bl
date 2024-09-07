package ru.itmo.hub.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
public class ServiceException extends RuntimeException {
    private final HttpStatus httpStatus;

    private ServiceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ServiceException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public static ServiceException videoNotFound(String message) {
        return new ServiceException(message, HttpStatus.NOT_FOUND);
    }

    public static ServiceException videoNotFound(Long videoId) {
        return new ServiceException(String.format("Video with id=%s not found.", videoId), HttpStatus.NOT_FOUND);
    }

    public static ServiceException commentNotFound(Long commentId) {
        return new ServiceException(String.format("Comment with id=%d does not exist.", commentId), HttpStatus.NOT_FOUND);
    }

    public static ServiceException playlistNotFound(Long playlistId) {
        return new ServiceException(String.format("Playlist with id=%d does not exist.", playlistId), HttpStatus.NOT_FOUND);
    }

    public static ServiceException gettingUnauthorizedUsername() {
        return new ServiceException("", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ServiceException illegalPageNumber() {
        return new ServiceException("Invalid page number - should be >= 1", HttpStatus.BAD_REQUEST);
    }

    public static ServiceException wrongUser() {
        return new ServiceException("Wrong user", HttpStatus.FORBIDDEN);
    }

    public static ServiceException unexpectedError() {
        return new ServiceException("Unexpected error happened at " + Instant.now().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ServiceException tagDoesNotExists() {
        return new ServiceException("Tag does not exist", HttpStatus.NOT_FOUND);
    }

    public static ServiceException tagSuggestionAlreadyExists() {
        return new ServiceException("Tag suggestion already exists", HttpStatus.CONFLICT);
    }

    public static ServiceException tagSuggestionDosNotExists() {
        return new ServiceException("Tag suggestion does not exist", HttpStatus.NOT_FOUND);
    }

}
