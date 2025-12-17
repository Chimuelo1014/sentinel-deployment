package com.sentinel.auth.listener;

import com.sentinel.auth.entity.UserEntity;
import com.sentinel.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthBillingListener {

    private final UserRepository userRepository;

    @RabbitListener(queues = "auth.billing.subscription.queue")
    @Transactional
    public void handleSubscriptionEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            log.info("📥 Received billing event in Auth Service: {}", eventType);

            String userIdStr = (String) event.get("userId");
            String planName = (String) event.get("planId"); // Billings send Plan Name as planId field now

            if (userIdStr == null || planName == null) {
                log.warn("⚠️ Event missing userId or planId: {}", event);
                return;
            }

            UUID userId = UUID.fromString(userIdStr);

            // For safety, uppercase the plan name (e.g., "Standard" -> "STANDARD")
            String normalizedPlan = planName.toUpperCase();

            // Convert to Enum
            com.sentinel.auth.enums.UserPlan planEnum;
            try {
                planEnum = com.sentinel.auth.enums.UserPlan.valueOf(normalizedPlan);
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Unknown plan: {}. Defaulting to FREE.", normalizedPlan);
                planEnum = com.sentinel.auth.enums.UserPlan.FREE;
            }

            UserEntity user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                log.warn("⚠️ User not found for plan update: {}", userId);
                return;
            }

            log.info("Updating user {} plan from {} to {}", userId, user.getPlan(), planEnum);
            user.setPlan(planEnum);
            userRepository.save(user);

            log.info("✅ User plan synchronized successfully.");

        } catch (Exception e) {
            log.error("❌ Error handling billing event in Auth Service: {}", e.getMessage(), e);
        }
    }
}
