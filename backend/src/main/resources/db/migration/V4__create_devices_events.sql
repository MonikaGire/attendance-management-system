CREATE TABLE devices (
    id CHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    api_key_hash VARCHAR(255) NOT NULL,
    last_heartbeat DATETIME(3),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted_at DATETIME(3),
    PRIMARY KEY (id),
    KEY idx_devices_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE device_events (
    id CHAR(36) NOT NULL,
    device_id CHAR(36) NOT NULL,
    biometric_id VARCHAR(50) NOT NULL,
    raw_timestamp DATETIME(3) NOT NULL,
    processed TINYINT(1) DEFAULT 0,
    attendance_record_id CHAR(36),
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_de_device_ts (device_id, raw_timestamp),
    KEY idx_de_biometric_ts (biometric_id, raw_timestamp),
    KEY idx_de_processed (processed),
    CONSTRAINT fk_de_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
