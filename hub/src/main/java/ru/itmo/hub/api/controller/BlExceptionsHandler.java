package ru.itmo.hub.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.itmo.hub.exception.ServiceException;

@Slf4j
@ControllerAdvice
public class BlExceptionsHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler
    protected ResponseEntity<Object> handleService(ServiceException ex, WebRequest req) {
        log.error(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), ex.getHttpStatus(), req);
    }
}
