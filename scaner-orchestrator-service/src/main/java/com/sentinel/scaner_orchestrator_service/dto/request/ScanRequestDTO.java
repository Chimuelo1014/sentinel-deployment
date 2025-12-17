package com.sentinel.scaner_orchestrator_service.dto.request;

import com.sentinel.scaner_orchestrator_service.domain.enums.ScanType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScanRequestDTO {
    @NotNull(message = "Scan type is required")
    private ScanType type;

    private String targetUrl;
    private String targetRepo;
    private java.util.UUID projectId;
    private String gitToken; // Optional
}
