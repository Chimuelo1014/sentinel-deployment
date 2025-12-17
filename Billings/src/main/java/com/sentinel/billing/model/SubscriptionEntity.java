package com.sentinel.billing.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false, updatable = false)
    private String id; // ej: sub_...

    @Column(name = "tenant_id", length = 64, nullable = false)
    private String tenantId;

    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    @Column(name = "plan_id", length = 32, nullable = false)
    private String planId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private SubscriptionStatus status; // ACTIVE, CANCELED, etc.

    @Column(name = "current_period_start", nullable = false)
    private OffsetDateTime currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private OffsetDateTime currentPeriodEnd;

    @Column(name = "cancel_at")
    private OffsetDateTime cancelAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public SubscriptionEntity() {
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // ---------- getters & setters fluídos ----------

    public String getId() {
        return id;
    }

    public SubscriptionEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public SubscriptionEntity setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public SubscriptionEntity setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getPlanId() {
        return planId;
    }

    public SubscriptionEntity setPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public SubscriptionEntity setStatus(SubscriptionStatus status) {
        this.status = status;
        return this;
    }

    public OffsetDateTime getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    public SubscriptionEntity setCurrentPeriodStart(OffsetDateTime currentPeriodStart) {
        this.currentPeriodStart = currentPeriodStart;
        return this;
    }

    public OffsetDateTime getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public SubscriptionEntity setCurrentPeriodEnd(OffsetDateTime currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
        return this;
    }

    public OffsetDateTime getCancelAt() {
        return cancelAt;
    }

    public SubscriptionEntity setCancelAt(OffsetDateTime cancelAt) {
        this.cancelAt = cancelAt;
        return this;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public SubscriptionEntity setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public SubscriptionEntity setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
