package com.sentinel.scaner_orchestrator_service.dto.message;

import com.sentinel.scaner_orchestrator_service.domain.enums.ScanType;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class ScanEventDTO {
    private UUID scanId;
    private ScanType requestedService;
    private String targetUrl;
    private String targetRepo;
    private String clientGitToken;
    private UUID tenantId;
}
