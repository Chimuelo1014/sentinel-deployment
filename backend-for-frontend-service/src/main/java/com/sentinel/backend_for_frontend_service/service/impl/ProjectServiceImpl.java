package com.sentinel.backend_for_frontend_service.service.impl;

import com.sentinel.backend_for_frontend_service.client.ProjectClient;
import com.sentinel.backend_for_frontend_service.dto.ProjectDto;
import com.sentinel.backend_for_frontend_service.service.ProjectService;
import com.sentinel.backend_for_frontend_service.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectClient projectClient;
    private final JwtUtils jwtUtils;

    @Override
    public Page<ProjectDto> listProjects(Pageable pageable, String token, String tenantId) {
        log.info("📁 Service: Listing projects");
        try {
            return projectClient.listProjects(pageable, token, tenantId);
        } catch (Exception e) {
            log.error("Error listing projects", e);
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    @Override
    public ProjectDto getProjectDetails(String projectId, String token, String tenantId) {
        log.info("🔍 Service: Getting project details: {}", projectId);
        try {
            return projectClient.getProjectDetails(projectId, token, tenantId);
        } catch (Exception e) {
            log.error("Error getting project details", e);
            return ProjectDto.builder().build();
        }
    }

    @Override
    public ProjectDto createProject(ProjectDto projectDto, String token, String tenantId) {
        log.info("➕ Service: Creating project: {}", projectDto.getName());
        try {
            String userId = jwtUtils.extractUserId(token);
            return projectClient.createProject(projectDto, token, tenantId, userId);
        } catch (Exception e) {
            log.error("Error creating project", e);
            return projectDto;
        }
    }

    @Override
    public ProjectDto updateProject(String projectId, ProjectDto projectDto, String token, String tenantId) {
        log.info("✏️ Service: Updating project: {}", projectId);
        try {
            String userId = jwtUtils.extractUserId(token);
            return projectClient.updateProject(projectId, projectDto, token, tenantId, userId);
        } catch (Exception e) {
            log.error("Error updating project", e);
            return projectDto;
        }
    }

    @Override
    public Map<String, String> deleteProject(String projectId, String token, String tenantId) {
        log.info("🗑️ Service: Deleting project: {}", projectId);
        try {
            String userId = jwtUtils.extractUserId(token);
            projectClient.deleteProject(projectId, token, tenantId, userId);
            return Map.of("message", "Project deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting project", e);
            return Map.of("error", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getProjectStatistics(String projectId, String token, String tenantId) {
        log.info("📊 Service: Getting project statistics: {}", projectId);
        try {
            return projectClient.getProjectStatistics(projectId, token, tenantId);
        } catch (Exception e) {
            log.error("Error getting project statistics", e);
            return Map.of("error", e.getMessage());
        }
    }
}
