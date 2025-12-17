package com.sentinel.scaner_orchestrator_service.repository;

import com.sentinel.scaner_orchestrator_service.domain.ScanJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ScanJobRepository extends JpaRepository<ScanJob, UUID> {
    Page<ScanJob> findByTenantId(UUID tenantId, Pageable pageable);

    Page<ScanJob> findByUserId(UUID userId, Pageable pageable);
}
