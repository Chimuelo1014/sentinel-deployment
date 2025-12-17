package com.sentinel.scaner_orchestrator_service.messaging;

import com.sentinel.scaner_orchestrator_service.domain.ScanJob;
import com.sentinel.scaner_orchestrator_service.domain.enums.ScanStatus;
import com.sentinel.scaner_orchestrator_service.repository.ScanJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScanListener {

    private final ScanJobRepository scanJobRepository;

    @RabbitListener(queues = "${app.rabbitmq.queue.scan-events}")
    public void handleScanEvent(Map<String, Object> message) {
        try {
            String scanIdStr = (String) message.get("scanId");
            if (scanIdStr == null)
                return;

            UUID scanId = UUID.fromString(scanIdStr);
            ScanJob job = scanJobRepository.findById(scanId).orElse(null);
            if (job == null) {
                log.warn("Received event for unknown scanId: {}", scanId);
                return;
            }

            String statusStr = (String) message.get("status");
            if (statusStr != null) {
                switch (statusStr.toUpperCase()) {
                    case "RUNNING":
                        job.setStatus(ScanStatus.RUNNING);
                        job.setStartedAt(LocalDateTime.now());
                        break;
                    case "COMPLETED":
                        job.setStatus(ScanStatus.COMPLETED);
                        job.setFinishedAt(LocalDateTime.now());
                        break;
                    case "FAILED":
                        job.setStatus(ScanStatus.FAILED);
                        job.setFinishedAt(LocalDateTime.now());
                        job.setFailureReason((String) message.get("reason"));
                        break;
                }
                scanJobRepository.save(job);
                log.info("Updated scan {} status to {}", scanId, statusStr);
            }
        } catch (Exception e) {
            log.error("Error processing scan event", e);
        }
    }
}
