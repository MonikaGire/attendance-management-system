package com.attendance.service;

import com.attendance.entity.AuditLog;
import com.attendance.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async("notificationExecutor")
    public void log(String userId, String action, String tableName,
                    String recordId, String oldValue, String newValue, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .tableName(tableName)
                .recordId(recordId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(log);
    }
}
