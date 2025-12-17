package com.sentinel.backend_for_frontend_service.service;

import com.sentinel.backend_for_frontend_service.dto.ProjectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ProjectService {
    Page<ProjectDto> listProjects(Pageable pageable, String token, String tenantId);
    ProjectDto getProjectDetails(String projectId, String token, String tenantId);
    ProjectDto createProject(ProjectDto projectDto, String token, String tenantId);
    ProjectDto updateProject(String projectId, ProjectDto projectDto, String token, String tenantId);
    Map<String, String> deleteProject(String projectId, String token, String tenantId);
    Map<String, Object> getProjectStatistics(String projectId, String token, String tenantId);
}
