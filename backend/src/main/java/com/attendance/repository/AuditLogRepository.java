package com.attendance.repository;

import com.attendance.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findByTableNameAndRecordId(String tableName, String recordId);
    Page<AuditLog> findByUserId(String userId, Pageable pageable);
}
