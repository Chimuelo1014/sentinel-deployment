package com.sentinel.scaner_orchestrator_service.messaging;

import com.sentinel.scaner_orchestrator_service.dto.message.ScanEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScanPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.scan}")
    private String scanExchange;

    @Value("${app.rabbitmq.routing-key.scan-requested}")
    private String scanRequestedRoutingKey;

    public void publishScanRequested(ScanEventDTO event) {
        log.info("Publishing scan.requested event: {}", event);
        try {
            rabbitTemplate.convertAndSend(scanExchange, scanRequestedRoutingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish scan event", e);
            throw new RuntimeException("Failed to queue scan job");
        }
    }
}
