package com.sentinel.billing.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false, updatable = false)
    private String id; // ej: pay_...

    @Column(name = "provider", length = 32, nullable = false)
    private String provider; // MERCADOPAGO, PAYPAL

    @Column(name = "external_payment_id", length = 128)
    private String externalPaymentId;

    @Column(name = "tenant_id", length = 64, nullable = false)
    private String tenantId;

    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    @Column(name = "plan_id", length = 32, nullable = false)
    private String planId; // BASIC, STANDARD, PRO, ENTERPRISE

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 8, nullable = false)
    private String currency; // USD, COP (de momento USD)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private PaymentStatus status; // PENDING, SUCCEEDED, etc.

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    public PaymentEntity() {
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

    // ---------- getters & setters en modo fluido ----------

    public String getId() {
        return id;
    }

    public PaymentEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getProvider() {
        return provider;
    }

    public PaymentEntity setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public String getExternalPaymentId() {
        return externalPaymentId;
    }

    public PaymentEntity setExternalPaymentId(String externalPaymentId) {
        this.externalPaymentId = externalPaymentId;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public PaymentEntity setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public PaymentEntity setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getPlanId() {
        return planId;
    }

    public PaymentEntity setPlanId(String planId) {
        this.planId = planId;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentEntity setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentEntity setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentEntity setStatus(PaymentStatus status) {
        this.status = status;
        return this;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public PaymentEntity setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public PaymentEntity setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public PaymentEntity setPaidAt(OffsetDateTime paidAt) {
        this.paidAt = paidAt;
        return this;
    }
}
