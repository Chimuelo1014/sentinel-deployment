package com.sentinel.billing.repository;

import com.sentinel.billing.model.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, String> {

        Optional<SubscriptionEntity> findFirstByTenantIdAndUserIdOrderByCreatedAtDesc(
                        String tenantId,
                        String userId);

        Optional<SubscriptionEntity> findFirstByTenantIdOrderByCreatedAtDesc(String tenantId);

        /**
         * Encuentra la suscripción de un tenant con un estado específico.
         * Para obtener la suscripción ACTIVA de un tenant.
         */
        Optional<SubscriptionEntity> findByTenantIdAndStatus(String tenantId,
                        com.sentinel.billing.model.SubscriptionStatus status);

        Optional<SubscriptionEntity> findFirstByUserIdOrderByCreatedAtDesc(String userId);

        /**
         * Find active subscription by userId (for MercadoPagoService)
         */
        Optional<SubscriptionEntity> findByUserId(String userId);
}
