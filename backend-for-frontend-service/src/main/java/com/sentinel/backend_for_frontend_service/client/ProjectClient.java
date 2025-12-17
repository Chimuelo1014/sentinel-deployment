package com.sentinel.backend_for_frontend_service.client;

import com.sentinel.backend_for_frontend_service.dto.ProjectDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "project-client", url = "${app.services.project-url:http://localhost:8083}")
public interface ProjectClient {

        @GetMapping("/api/projects")
        Page<ProjectDto> listProjects(
                        Pageable pageable,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId);

        @GetMapping("/api/projects/{projectId}")
        ProjectDto getProjectDetails(
                        @PathVariable String projectId,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId);

        @PostMapping("/api/projects")
        ProjectDto createProject(
                        @RequestBody ProjectDto projectDto,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
                        @RequestHeader("X-User-Id") String userId);

        @PutMapping("/api/projects/{projectId}")
        ProjectDto updateProject(
                        @PathVariable String projectId,
                        @RequestBody ProjectDto projectDto,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
                        @RequestHeader("X-User-Id") String userId);

        @DeleteMapping("/api/projects/{projectId}")
        void deleteProject(
                        @PathVariable String projectId,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
                        @RequestHeader("X-User-Id") String userId);

        @GetMapping("/api/projects/{projectId}/statistics")
        Map<String, Object> getProjectStatistics(
                        @PathVariable String projectId,
                        @RequestHeader("Authorization") String token,
                        @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId);

        // Legacy method for Dashboard (compatibility)
        @GetMapping("/api/projects")
        List<Map<String, Object>> getMyProjects(@RequestHeader("Authorization") String token);

        // New method for tenant-specific projects
        @GetMapping("/api/projects")
        List<Map<String, Object>> getProjectsByTenant(
                        @RequestHeader("Authorization") String token,
                        @RequestParam("tenantId") String tenantId);
}
