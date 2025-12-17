package com.sentinel.backend_for_frontend_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponseDto {
    private String scanId;
    private String status;
    private String requestedService;
    private String acceptanceTimestampUtc;
    private String completionMethod;
    private Integer estimatedCompletionTime;
    private String message;
}
