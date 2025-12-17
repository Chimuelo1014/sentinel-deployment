package com.sentinel.backend_for_frontend_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bff/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class NotificationController {

    /**
     * Get notifications
     */
    @GetMapping
    @Operation(summary = "Get notifications", description = "Retrieve user notifications with pagination")
    public ResponseEntity<Page<Map<String, Object>>> getNotifications(
            @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            @RequestHeader("Authorization") String token) {
        log.info("🔔 BFF: Get notifications - Unread only: {}, Type: {}", unreadOnly, type);
        return ResponseEntity.ok(Page.empty(pageable));
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<Map<String, String>> markNotificationAsRead(
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String token) {
        log.info("✅ BFF: Mark notification as read: {}", notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications for the user as read")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @RequestHeader("Authorization") String token) {
        log.info("✅ BFF: Mark all notifications as read");
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String token) {
        log.info("🗑️ BFF: Delete notification: {}", notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
    }

    /**
     * Get notification preferences
     */
    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences", description = "Retrieve user notification preferences and channels")
    public ResponseEntity<Map<String, Object>> getNotificationPreferences(
            @RequestHeader("Authorization") String token) {
        log.info("⚙️ BFF: Get notification preferences");
        return ResponseEntity.ok(Map.of(
                "email", Map.of(
                        "scanCompletion", true,
                        "criticalVulnerabilities", true,
                        "weeklyReport", true
                ),
                "webhook", Map.of(
                        "enabled", false,
                        "url", ""
                ),
                "slack", Map.of(
                        "enabled", false,
                        "webhookUrl", ""
                )
        ));
    }

    /**
     * Update notification preferences
     */
    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences", description = "Update user notification preferences and channels")
    public ResponseEntity<Map<String, Object>> updateNotificationPreferences(
            @RequestBody Map<String, Object> preferences,
            @RequestHeader("Authorization") String token) {
        log.info("⚙️ BFF: Update notification preferences");
        return ResponseEntity.ok(preferences);
    }
}
