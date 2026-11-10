package com.nectux.magicgold.audit.repository;

import com.nectux.magicgold.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);
}
