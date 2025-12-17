package com.sentinel.backend_for_frontend_service.controller;

import com.sentinel.backend_for_frontend_service.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments-history")
@RequiredArgsConstructor
public class PaymentsHistoryController {

    private final JwtUtils jwtUtils;

    @GetMapping("/me")
    public ResponseEntity<?> getMyPaymentsHistory(@RequestHeader("Authorization") String token) {
        log.info("💰 BFF: Getting user's payment history...");

        // Return empty list for now - can be connected to billing service later
        return ResponseEntity.ok(List.of());
    }

    @GetMapping
    public ResponseEntity<?> getPaymentsHistory(@RequestHeader("Authorization") String token) {
        log.info("💰 BFF: Getting payment history...");

        // Return empty list for now
        return ResponseEntity.ok(List.of());
    }
}
