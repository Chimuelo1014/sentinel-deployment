package com.sentinel.scaner_orchestrator_service.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class TenantDTO {
    private UUID id;
    private String name;
    private String plan;
}
