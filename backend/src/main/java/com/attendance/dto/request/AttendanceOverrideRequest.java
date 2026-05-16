package com.attendance.dto.request;

import com.attendance.enums.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceOverrideRequest {
    @NotBlank private String studentId;
    @NotBlank private String sessionId;
    @NotNull  private AttendanceStatus newStatus;
    private String notes;
}
