package com.sentinel.billing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Feign Client para comunicarse con Project-Service.
 * Permite obtener información de proyectos.
 */
@FeignClient(
        name = "projectService",
        url = "${services.project.url:http://localhost:8084}"
)
public interface ProjectServiceClient {

    @GetMapping("/api/internal/projects/{projectId}")
    Map<String, Object> getProject(@PathVariable("projectId") String projectId);

    @GetMapping("/api/internal/tenants/{tenantId}/projects")
    Map<String, Object> getTenantProjects(@PathVariable("tenantId") String tenantId);
}
