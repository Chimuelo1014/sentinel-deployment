package com.sentinel.scaner_orchestrator_service.dto.response;

import com.sentinel.scaner_orchestrator_service.domain.enums.ScanStatus;
import com.sentinel.scaner_orchestrator_service.domain.enums.ScanType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ScanResponseDTO {
    private UUID id;
    private UUID tenantId;
    private UUID projectId; // Added for frontend filtering
    private ScanType type;
    private ScanStatus status;
    private String targetUrl;
    private String targetRepo;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
    private String failureReason;
}
