package com.sentinel.results_aggregator_service.domain;

import com.sentinel.results_aggregator_service.domain.enums.ScanStatus;
import com.sentinel.results_aggregator_service.domain.enums.ScanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "scan_results")
public class ScanResult {

    @Id
    private String id;

    @Indexed(unique = true)
    private UUID scanId;

    private UUID tenantId;

    private ScanType type;

    private ScanStatus status;

    private List<Vulnerability> findings;

    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;

    private LocalDateTime detectedAt;
    private LocalDateTime completedAt;
}
