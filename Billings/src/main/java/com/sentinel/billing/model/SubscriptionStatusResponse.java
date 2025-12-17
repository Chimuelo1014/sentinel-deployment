package com.sentinel.billing.model;

import java.time.OffsetDateTime;

/**
 * Respuesta simple para consultar el estado de la suscripción actual.
 *
 * Más adelante esto vendrá de una base de datos usando userId/tenantId del JWT.
 */
public class SubscriptionStatusResponse {

    private String userId;
    private String tenantId;
    private String planId;
    private String status;           // ACTIVE, EXPIRED, CANCELLED, PAST_DUE, etc.
    private OffsetDateTime nextBillingDate;

    public SubscriptionStatusResponse() {
    }

    public SubscriptionStatusResponse(String userId,
                                      String tenantId,
                                      String planId,
                                      String status,
                                      OffsetDateTime nextBillingDate) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.planId = planId;
        this.status = status;
        this.nextBillingDate = nextBillingDate;
    }

    public String getUserId() {
        return userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPlanId() {
        return planId;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getNextBillingDate() {
        return nextBillingDate;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNextBillingDate(OffsetDateTime nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }
}
