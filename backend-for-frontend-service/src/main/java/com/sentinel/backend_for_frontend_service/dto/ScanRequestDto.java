package com.sentinel.backend_for_frontend_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanRequestDto {
    private String projectId;
    private List<String> scanTypes;
    private String targetUrl;
    private String clientGitToken;
    private String branch;
    private String commitSha;
}
