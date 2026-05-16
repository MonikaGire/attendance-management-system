package com.attendance.repository;

import com.attendance.entity.DeviceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface DeviceEventRepository extends JpaRepository<DeviceEvent, String> {

    @Query("SELECT COUNT(de) FROM DeviceEvent de WHERE de.biometricId = :biometricId " +
           "AND de.device.id = :deviceId " +
           "AND de.rawTimestamp BETWEEN :from AND :to " +
           "AND de.processed = true")
    long countDuplicateEvents(@Param("biometricId") String biometricId,
                               @Param("deviceId") String deviceId,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);
}
