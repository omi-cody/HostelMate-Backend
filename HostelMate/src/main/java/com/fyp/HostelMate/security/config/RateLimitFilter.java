package com.fyp.HostelMate.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple sliding-window rate limiter on /api/auth/** endpoints.
 * Allows MAX_REQUESTS per IP per WINDOW_SECONDS.
 * Replace with Redis-backed solution for horizontal scaling.
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int    MAX_REQUESTS     = 20;
    private static final long   WINDOW_SECONDS   = 60;

    private record RequestCount(long windowStart, int count) {}

    private final Map<String, RequestCount> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String ip = getClientIp(request);
        long now = Instant.now().getEpochSecond();

        RequestCount current = requestCounts.compute(ip, (key, existing) -> {
            if (existing == null || now - existing.windowStart() >= WINDOW_SECONDS) {
                return new RequestCount(now, 1);
            }
            return new RequestCount(existing.windowStart(), existing.count() + 1);
        });

        if (current.count() > MAX_REQUESTS) {
            log.warn("Rate limit exceeded for IP={}", ip);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
