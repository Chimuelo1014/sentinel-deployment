package com.sentinel.tenant_service.repository;

import com.sentinel.tenant_service.entity.InvitationProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvitationProjectRepository extends JpaRepository<InvitationProjectEntity, UUID> {

    List<InvitationProjectEntity> findByInvitationId(UUID invitationId);

    void deleteByInvitationId(UUID invitationId);
}
