package com.sentinel.billing.repository;

import com.sentinel.billing.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {

    List<PaymentEntity> findByTenantIdAndUserIdOrderByCreatedAtDesc(
            String tenantId,
            String userId);

    /**
     * Find payment by external provider ID (MercadoPago payment ID)
     */
    Optional<PaymentEntity> findByExternalPaymentId(String externalPaymentId);

    /**
     * Find payments by userId for payment history
     */
    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
