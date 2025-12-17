package com.sentinel.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Prevent MIME sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        // Basic XSS protection (older browsers)
        response.setHeader("X-XSS-Protection", "1; mode=block");
        // Referrer policy
        response.setHeader("Referrer-Policy", "no-referrer");
        // Permissions policy (minimal)
        response.setHeader("Permissions-Policy", "geolocation=()");
        // HSTS (enforce HTTPS for 1 year)
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        // Content Security Policy - conservative default
        response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none';");

        filterChain.doFilter(request, response);
    }
}
