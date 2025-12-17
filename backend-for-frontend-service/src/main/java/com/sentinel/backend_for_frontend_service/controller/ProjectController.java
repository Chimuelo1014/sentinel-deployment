package com.sentinel.backend_for_frontend_service.controller;

import com.sentinel.backend_for_frontend_service.dto.ProjectDto;
import com.sentinel.backend_for_frontend_service.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bff/projects")
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * List all projects
     */
    @GetMapping
    @Operation(summary = "List projects", description = "Retrieve paginated list of all projects for the tenant")
    public ResponseEntity<Page<ProjectDto>> listProjects(
            @PageableDefault(size = 20, page = 0, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("📁 BFF: List projects - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(projectService.listProjects(pageable, token, tenantId));
    }

    /**
     * Get project details
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "Get project details", description = "Retrieve detailed information about a specific project")
    public ResponseEntity<ProjectDto> getProject(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("🔍 BFF: Get project details: {}", projectId);
        return ResponseEntity.ok(projectService.getProjectDetails(projectId, token, tenantId));
    }

    /**
     * Create a new project
     */
    @PostMapping
    @Operation(summary = "Create project", description = "Create a new project for the tenant")
    public ResponseEntity<ProjectDto> createProject(
            @RequestBody ProjectDto projectDto,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("➕ BFF: Create project: {}", projectDto.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(projectDto, token, tenantId));
    }

    /**
     * Update project
     */
    @PutMapping("/{projectId}")
    @Operation(summary = "Update project", description = "Update an existing project")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable String projectId,
            @RequestBody ProjectDto projectDto,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("✏️ BFF: Update project: {}", projectId);
        return ResponseEntity.ok(projectService.updateProject(projectId, projectDto, token, tenantId));
    }

    /**
     * Delete project
     */
    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete project", description = "Delete a project and its associated scans")
    public ResponseEntity<Map<String, String>> deleteProject(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("🗑️ BFF: Delete project: {}", projectId);
        return ResponseEntity.ok(projectService.deleteProject(projectId, token, tenantId));
    }

    /**
     * Get project statistics
     */
    @GetMapping("/{projectId}/statistics")
    @Operation(summary = "Get project statistics", description = "Retrieve statistics and metrics for a project")
    public ResponseEntity<Map<String, Object>> getProjectStatistics(
            @PathVariable String projectId,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        log.info("📊 BFF: Get project statistics: {}", projectId);
        return ResponseEntity.ok(projectService.getProjectStatistics(projectId, token, tenantId));
    }
}
