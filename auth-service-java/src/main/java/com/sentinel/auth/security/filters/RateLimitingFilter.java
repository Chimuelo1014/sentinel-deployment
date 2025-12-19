package com.sentinel.auth.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight in-memory rate limiter without external libraries.
 * - Per-IP simple token-bucket counters.
 * - Limits are conservative and suitable for low-traffic dev/test environments.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static class TokenBucket {
        long lastRefill;
        double tokens;
        final double capacity;
        final double refillPerSecond;

        TokenBucket(double capacity, double refillPerSecond) {
            this.capacity = capacity;
            this.refillPerSecond = refillPerSecond;
            this.tokens = capacity;
            this.lastRefill = Instant.now().getEpochSecond();
        }

        synchronized boolean tryConsume(double amount) {
            long now = Instant.now().getEpochSecond();
            long delta = now - lastRefill;
            if (delta > 0) {
                tokens = Math.min(capacity, tokens + delta * refillPerSecond);
                lastRefill = now;
            }
            if (tokens >= amount) {
                tokens -= amount;
                return true;
            }
            return false;
        }
    }

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private TokenBucket bucketFor(String key, double capacity, double refillPerSecond) {
        return buckets.computeIfAbsent(key, k -> new TokenBucket(capacity, refillPerSecond));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip rate limiting for HEAD, GET, OPTIONS requests
        if (method != null && !method.equalsIgnoreCase("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isProtectedPath(path) && "POST".equalsIgnoreCase(method)) {
            String ip = extractClientIp(request);
            String key = ip + ":" + path;
            TokenBucket bucket = isLoginPath(path)
                    ? bucketFor(key, 10, 10.0 / 60.0) // 10 tokens, refill 10 per minute
                    : bucketFor(key, 30, 30.0 / 60.0); // 30 tokens per minute

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path) {
        return path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")
                || path.startsWith("/api/auth/refresh") || path.startsWith("/oauth2");
    }

    private boolean isLoginPath(String path) {
        return path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register");
    }

    private String extractClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty())
            return xff.split(",")[0].trim();
        String ip = req.getRemoteAddr();
        return ip == null ? "unknown" : ip;
    }
}
