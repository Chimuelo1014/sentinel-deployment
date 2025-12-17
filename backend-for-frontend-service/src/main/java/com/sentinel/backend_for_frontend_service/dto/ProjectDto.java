package com.sentinel.backend_for_frontend_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    @JsonAlias("id")
    private String projectId;
    private String name;
    private String description;
    private String repositoryUrl;
    private String language;
    private String owner;
    private String createdAt;
    private String lastScanDate;
    private Integer scanCount;
    private String tenantId;
    private Map<String, Object> statistics;
}
