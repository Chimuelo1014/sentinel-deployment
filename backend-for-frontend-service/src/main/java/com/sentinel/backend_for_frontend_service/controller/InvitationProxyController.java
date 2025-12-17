package com.sentinel.backend_for_frontend_service.controller;

import com.sentinel.backend_for_frontend_service.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Proxy controller for tenant invitation operations
 * Routes requests to tenant-service
 */
@Slf4j
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class InvitationProxyController {

    private final RestTemplate restTemplate;
    private final JwtUtils jwtUtils;

    @Value("${services.tenant.url}")
    private String tenantServiceUrl;

    /**
     * Get all invitations for a tenant
     * GET /api/tenants/{tenantId}/invitations
     */
    @GetMapping("/{tenantId}/invitations")
    public ResponseEntity<?> getInvitations(
            @PathVariable UUID tenantId,
            @RequestHeader("Authorization") String authHeader) {

        log.debug("Proxying GET invitations for tenant: {}", tenantId);

        String userId = jwtUtils.extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token - could not extract user ID");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("X-User-Id", userId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = tenantServiceUrl + "/api/tenants/" + tenantId + "/invitations";

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("Error proxying GET invitations: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching invitations: " + e.getMessage());
        }
    }

    /**
     * Create a new invitation
     * POST /api/tenants/{tenantId}/invitations
     */
    @PostMapping("/{tenantId}/invitations")
    public ResponseEntity<?> createInvitation(
            @PathVariable UUID tenantId,
            @RequestBody Object invitationRequest,
            @RequestHeader("Authorization") String authHeader) {

        log.debug("Proxying POST invitation for tenant: {}", tenantId);

        String userId = jwtUtils.extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token - could not extract user ID");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("X-User-Id", userId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(invitationRequest, headers);

        String url = tenantServiceUrl + "/api/tenants/" + tenantId + "/invitations";

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Object.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // Preserve the original status code and response body from tenant-service
            log.warn("Tenant-service returned error status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error proxying POST invitation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating invitation: " + e.getMessage());
        }
    }

    /**
     * Cancel/delete an invitation
     * DELETE /api/tenants/invitations/{invitationId}
     */
    @DeleteMapping("/invitations/{invitationId}")
    public ResponseEntity<?> cancelInvitation(
            @PathVariable UUID invitationId,
            @RequestHeader("Authorization") String authHeader) {

        log.debug("Proxying DELETE invitation: {}", invitationId);

        String userId = jwtUtils.extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("X-User-Id", userId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = tenantServiceUrl + "/api/tenants/invitations/" + invitationId;

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    Void.class);
            return ResponseEntity.status(response.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Error proxying DELETE invitation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
