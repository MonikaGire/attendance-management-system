CREATE TABLE messages (
    id CHAR(36) NOT NULL,
    attendance_record_id CHAR(36),
    recipient_type VARCHAR(30) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    payload TEXT,
    status VARCHAR(20) DEFAULT 'QUEUED',
    attempts INT DEFAULT 0,
    sent_at DATETIME(3),
    error_message TEXT,
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_messages_status (status),
    KEY idx_messages_ar (attendance_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
    id CHAR(36) NOT NULL,
    user_id CHAR(36),
    action VARCHAR(100) NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    record_id VARCHAR(36),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_audit_user (user_id),
    KEY idx_audit_table_record (table_name, record_id),
    KEY idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
