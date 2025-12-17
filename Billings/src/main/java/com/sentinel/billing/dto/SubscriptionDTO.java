package com.sentinel.billing.dto;

import com.sentinel.billing.model.SubscriptionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO con información de suscripción de un tenant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {

    private String subscriptionId;
    private String tenantId;
    private String userId;
    private String planId;
    private String status;
    private OffsetDateTime currentPeriodStart;
    private OffsetDateTime currentPeriodEnd;

    /**
     * Crea un SubscriptionDTO desde un SubscriptionEntity.
     */
    public static SubscriptionDTO fromEntity(SubscriptionEntity entity) {
        return SubscriptionDTO.builder()
                .subscriptionId(entity.getId())
                .tenantId(entity.getTenantId())
                .userId(entity.getUserId())
                .planId(entity.getPlanId())
                .status(entity.getStatus().name())
                .currentPeriodStart(entity.getCurrentPeriodStart())
                .currentPeriodEnd(entity.getCurrentPeriodEnd())
                .build();
    }
}
