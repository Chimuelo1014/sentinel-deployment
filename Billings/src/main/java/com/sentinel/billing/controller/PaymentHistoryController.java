package com.sentinel.billing.controller;

import com.sentinel.billing.model.PaymentEntity;
import com.sentinel.billing.model.PaymentStatus;
import com.sentinel.billing.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Controller for payment history queries.
 */
@RestController
@RequestMapping("/api/payments-history")
// CORS handled globally by SecurityConfig
public class PaymentHistoryController {

    private final PaymentRepository paymentRepository;

    public PaymentHistoryController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Get payment history for current user
     * GET /api/payments-history/me
     */
    @GetMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @RequestHeader(value = "X-User-Id", required = false) String userIdStr) {

        if (userIdStr == null || userIdStr.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        // Query payments by userId directly (not by tenantId)
        List<PaymentEntity> entities = paymentRepository
                .findByUserIdOrderByCreatedAtDesc(userIdStr);

        List<PaymentResponse> response = entities.stream()
                .map(PaymentResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Alias for frontend compatibility - same as /me
     * GET /api/payments-history/history
     */
    @GetMapping("/history")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return getMyPayments(userId);
    }

    public static class PaymentResponse {
        private String id;
        private String provider;
        private String planId;
        private String description;
        private double amount;
        private String currency;
        private PaymentStatus status;
        private OffsetDateTime createdAt;
        private OffsetDateTime paidAt;

        public static PaymentResponse fromEntity(PaymentEntity entity) {
            PaymentResponse dto = new PaymentResponse();
            dto.id = entity.getId();
            dto.provider = entity.getProvider();
            dto.planId = entity.getPlanId();
            dto.description = "Plan " + entity.getPlanId() + " - " + entity.getCurrency();
            dto.amount = entity.getAmount().doubleValue();
            dto.currency = entity.getCurrency();
            dto.status = entity.getStatus();
            dto.createdAt = entity.getCreatedAt();
            dto.paidAt = entity.getPaidAt();
            return dto;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getPlanId() {
            return planId;
        }

        public void setPlanId(String planId) {
            this.planId = planId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public PaymentStatus getStatus() {
            return status;
        }

        public void setStatus(PaymentStatus status) {
            this.status = status;
        }

        public OffsetDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public OffsetDateTime getPaidAt() {
            return paidAt;
        }

        public void setPaidAt(OffsetDateTime paidAt) {
            this.paidAt = paidAt;
        }
    }
}
