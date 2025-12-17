package com.sentinel.tenant.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * Feign Client to communicate with Billing Service
 */
@FeignClient(name = "billing-service", url = "${services.billing-url:http://billing-service:8084}")
public interface BillingClient {

    /**
     * Get user's subscription (includes plan information)
     */
    @GetMapping("/api/subscriptions/me")
    Map<String, Object> getUserSubscription(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-User-Id") String userId);

    /**
     * Get plan details by ID
     */
    @GetMapping("/api/plans/{planId}")
    Map<String, Object> getPlanById(
            @PathVariable("planId") String planId,
            @RequestHeader("Authorization") String token);
}
