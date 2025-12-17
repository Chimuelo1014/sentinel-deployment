package com.sentinel.backend_for_frontend_service.controller;

import com.sentinel.backend_for_frontend_service.dto.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bff/users")
@Tag(name = "Users", description = "User profile and preferences endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {

    /**
     * Get user profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Retrieve the current user's profile information")
    public ResponseEntity<UserProfileDto> getUserProfile(
            @RequestHeader("Authorization") String token) {
        log.info("👤 BFF: Get user profile");
        return ResponseEntity.ok(UserProfileDto.builder()
                .userId("user-id-from-token")
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("https://...")
                .role("ADMIN")
                .createdAt("2025-01-01T10:00:00Z")
                .build());
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update the current user's profile information")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @RequestBody UserProfileDto profileDto,
            @RequestHeader("Authorization") String token) {
        log.info("✏️ BFF: Update user profile");
        return ResponseEntity.ok(profileDto);
    }

    /**
     * Get user preferences
     */
    @GetMapping("/preferences")
    @Operation(summary = "Get user preferences", description = "Retrieve user notification and UI preferences")
    public ResponseEntity<Map<String, Object>> getUserPreferences(
            @RequestHeader("Authorization") String token) {
        log.info("⚙️ BFF: Get user preferences");
        return ResponseEntity.ok(Map.of(
                "notifications", Map.of(
                        "scanCompletion", true,
                        "criticalVulnerabilities", true,
                        "weeklyReport", true
                ),
                "theme", "DARK",
                "language", "en"
        ));
    }

    /**
     * Update user preferences
     */
    @PutMapping("/preferences")
    @Operation(summary = "Update user preferences", description = "Update user notification and UI preferences")
    public ResponseEntity<Map<String, Object>> updateUserPreferences(
            @RequestBody Map<String, Object> preferences,
            @RequestHeader("Authorization") String token) {
        log.info("⚙️ BFF: Update user preferences");
        return ResponseEntity.ok(preferences);
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change the user's password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> passwordData,
            @RequestHeader("Authorization") String token) {
        log.info("🔐 BFF: Change password");
        return ResponseEntity.ok(Map.of(
                "message", "Password changed successfully"
        ));
    }
}
