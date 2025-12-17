package com.sentinel.backend_for_frontend_service.service;

import com.sentinel.backend_for_frontend_service.dto.ScanRequestDto;
import com.sentinel.backend_for_frontend_service.dto.ScanResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ScanService {
    ScanResponseDto requestScan(ScanRequestDto requestDto, String token, String tenantId);
    Page<Map<String, Object>> listScans(Pageable pageable, String status, String type, String startDate, String endDate, String token, String tenantId);
    Map<String, Object> getScanStatus(String scanId, String token, String tenantId);
    Map<String, Object> getScanResults(String scanId, String token, String tenantId);
    Map<String, String> cancelScan(String scanId, String token, String tenantId);
    Map<String, Object> exportScanResults(String scanId, String format, String token, String tenantId);
}
