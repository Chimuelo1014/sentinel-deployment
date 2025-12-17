package com.sentinel.backend_for_frontend_service.controller;

import com.sentinel.backend_for_frontend_service.client.ProjectClient;
import com.sentinel.backend_for_frontend_service.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectProxyController {

    private final ProjectClient projectClient;
    private final JwtUtils jwtUtils;

    /**
     * Get all projects for a tenant
     */
    @GetMapping
    public ResponseEntity<?> getProjects(
            @RequestParam String tenantId,
            @RequestHeader("Authorization") String authHeader) {

        log.info("🔹 BFF: Getting projects for tenant: {}", tenantId);

        try {
            List<Map<String, Object>> projects = projectClient.getProjectsByTenant(authHeader, tenantId);
            log.info("✅ Found {} projects", projects.size());
            return ResponseEntity.ok(projects);
        } catch (feign.FeignException e) {
            log.error("❌ Error fetching projects: {} {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to fetch projects", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Get project by ID
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProjectById(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token) {

        log.info("🔍 BFF: Getting project: {}", projectId);

        try {
            var project = projectClient.getProjectDetails(projectId, token, null);
            return ResponseEntity.ok(project);
        } catch (feign.FeignException e) {
            log.error("❌ Error fetching project: {} {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to fetch project", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Create project
     */
    @PostMapping
    public ResponseEntity<?> createProject(
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestBody Map<String, Object> request) {

        log.info("➕ BFF: Creating project: {}", request.get("name"));

        try {
            // Convert request to ProjectDto - simplified approach
            com.sentinel.backend_for_frontend_service.dto.ProjectDto dto = new com.sentinel.backend_for_frontend_service.dto.ProjectDto();
            dto.setName((String) request.get("name"));
            dto.setDescription((String) request.get("description"));
            dto.setRepositoryUrl((String) request.get("repositoryUrl"));

            String userId = jwtUtils.extractUserId(token);
            var project = projectClient.createProject(dto, token, tenantId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(project);
        } catch (feign.FeignException e) {
            log.error("❌ Error creating project: {} {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to create project", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Update project
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<?> updateProject(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestBody Map<String, Object> request) {

        log.info("✏️ BFF: Updating project: {}", projectId);

        try {
            com.sentinel.backend_for_frontend_service.dto.ProjectDto dto = new com.sentinel.backend_for_frontend_service.dto.ProjectDto();
            dto.setName((String) request.get("name"));
            dto.setDescription((String) request.get("description"));
            dto.setRepositoryUrl((String) request.get("repositoryUrl"));

            String userId = jwtUtils.extractUserId(token);
            var project = projectClient.updateProject(projectId, dto, token, tenantId, userId);
            return ResponseEntity.ok(project);
        } catch (feign.FeignException e) {
            log.error("❌ Error updating project: {} {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to update project", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    /**
     * Delete project
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> deleteProject(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {

        log.info("🗑️ BFF: Deleting project: {}", projectId);

        try {
            String userId = jwtUtils.extractUserId(token);
            projectClient.deleteProject(projectId, token, tenantId, userId);
            return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
        } catch (feign.FeignException e) {
            log.error("❌ Error deleting project: {} {}", e.status(), e.getMessage());
            return ResponseEntity.status(e.status())
                    .body(Map.of("error", "Failed to delete project", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
}
