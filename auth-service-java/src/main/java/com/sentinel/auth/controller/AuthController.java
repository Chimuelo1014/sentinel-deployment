package com.sentinel.auth.controller;

import com.sentinel.auth.dto.request.LoginRequest;
import com.sentinel.auth.dto.request.RefreshTokenRequest;
import com.sentinel.auth.dto.request.RegisterRequest;
import com.sentinel.auth.dto.response.AuthResponse;
import com.sentinel.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST API Controller.
 * Exposes endpoints for:
 * - Register
 * - Login
 * - Refresh Access Token
 */
@RestController
@RequestMapping("/api/auth") // ✅ Corregido para coincidir con SecurityConfig
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Login endpoint.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Refresh token endpoint.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * Revoke a single refresh token (public endpoint).
     */
    @PostMapping("/revoke")
    public ResponseEntity<Void> revokeToken(@Valid @RequestBody RefreshTokenRequest request) {
        authService.revokeRefreshToken(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    /**
     * Logout current authenticated user by revoking all their refresh tokens.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logoutCurrentUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        Object principal = authentication.getPrincipal();
        java.util.UUID userId = null;
        if (principal instanceof com.sentinel.auth.entity.UserEntity) {
            userId = ((com.sentinel.auth.entity.UserEntity) principal).getId();
        }

        if (userId == null) return ResponseEntity.status(401).build();

        authService.revokeAllTokensForUser(userId);
        return ResponseEntity.noContent().build();
    }
}