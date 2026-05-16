package com.attendance.repository;

import com.attendance.entity.Device;
import com.attendance.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByStatus(DeviceStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.status = com.attendance.enums.DeviceStatus.OFFLINE WHERE d.lastHeartbeat < :threshold " +
           "AND d.status = com.attendance.enums.DeviceStatus.ACTIVE AND d.deletedAt IS NULL")
    int markOfflineDevices(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = com.attendance.enums.DeviceStatus.ACTIVE")
    long countActiveDevices();
}
