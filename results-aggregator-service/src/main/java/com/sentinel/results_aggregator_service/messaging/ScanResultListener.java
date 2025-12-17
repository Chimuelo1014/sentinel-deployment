package com.sentinel.results_aggregator_service.messaging;

import com.sentinel.results_aggregator_service.dto.ScanResultEventDTO;
import com.sentinel.results_aggregator_service.service.ResultsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScanResultListener {

    private final ResultsService resultsService;

    @RabbitListener(queues = "${app.rabbitmq.queue.scan-results}")
    public void handleScanResult(ScanResultEventDTO event) {
        log.info("Received scan completed event: {}", event);
        try {
            resultsService.processScanResult(event);
        } catch (Exception e) {
            log.error("Error processing scan result", e);
            // In a real scenario, might want to DLQ this
        }
    }
}
