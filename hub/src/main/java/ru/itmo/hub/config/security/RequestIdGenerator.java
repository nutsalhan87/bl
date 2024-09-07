package ru.itmo.hub.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class RequestIdGenerator implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID()
                .toString();

        MDC.put(RequestIdGenerator.class.getCanonicalName(), requestId);
        response.addHeader("X-Request-Id", requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(RequestIdGenerator.class.getCanonicalName());
    }

    public static String getRequestId() {
        return MDC.get(RequestIdGenerator.class.getCanonicalName());
    }
}
