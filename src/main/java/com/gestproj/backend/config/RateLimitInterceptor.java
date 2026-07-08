package com.gestproj.backend.config;

import com.gestproj.backend.common.exception.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final long RATE_LIMIT_WINDOW_MS = 60000;
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private final Map<String, RequestTracker> requestTracker = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdentifier = getUserIdentifier(request);
        RequestTracker tracker = requestTracker.computeIfAbsent(userIdentifier, k -> new RequestTracker());

        if (!tracker.allowRequest()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            throw new ForbiddenException("Rate limit exceeded: Maximum " + MAX_REQUESTS_PER_MINUTE + " requests per minute");
        }

        return true;
    }

    private String getUserIdentifier(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return request.getRemoteAddr();
    }

    private static class RequestTracker {
        private long lastResetTime = System.currentTimeMillis();
        private int requestCount = 0;

        synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            if (now - lastResetTime > RATE_LIMIT_WINDOW_MS) {
                lastResetTime = now;
                requestCount = 0;
            }

            if (requestCount < MAX_REQUESTS_PER_MINUTE) {
                requestCount++;
                return true;
            }
            return false;
        }
    }
}
