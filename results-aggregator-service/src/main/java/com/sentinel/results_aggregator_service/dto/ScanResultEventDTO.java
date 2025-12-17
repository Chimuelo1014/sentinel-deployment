package com.sentinel.results_aggregator_service.dto;

import com.sentinel.results_aggregator_service.domain.Vulnerability;
import com.sentinel.results_aggregator_service.domain.enums.ScanStatus;
import com.sentinel.results_aggregator_service.domain.enums.ScanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultEventDTO {
    private UUID scanId;
    private UUID tenantId;
    private ScanType type;
    private ScanStatus status;
    private List<Vulnerability> findings;
    private LocalDateTime completedAt;
}
