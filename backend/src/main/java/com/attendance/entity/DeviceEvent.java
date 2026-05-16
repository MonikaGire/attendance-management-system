package com.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEvent {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "biometric_id", nullable = false, length = 50)
    private String biometricId;

    @Column(name = "raw_timestamp", nullable = false, columnDefinition = "DATETIME(3)")
    private LocalDateTime rawTimestamp;

    @Column(name = "processed", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean processed;

    @Column(name = "attendance_record_id", columnDefinition = "CHAR(36)")
    private String attendanceRecordId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME(3)")
    private LocalDateTime createdAt;
}
