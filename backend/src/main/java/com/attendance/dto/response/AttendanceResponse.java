package com.attendance.dto.response;

import com.attendance.enums.AttendanceSource;
import com.attendance.enums.AttendanceStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private String id;
    private String studentId;
    private String studentName;
    private String biometricId;
    private String sessionId;
    private LocalDate sessionDate;
    private LocalTime sessionStartTime;
    private String className;
    private AttendanceStatus status;
    private LocalDateTime checkInTime;
    private AttendanceSource source;
    private String deviceId;
    private String notes;
    private String overriddenBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
