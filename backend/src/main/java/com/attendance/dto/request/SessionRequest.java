package com.attendance.dto.request;

import com.attendance.enums.SessionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SessionRequest {
    @NotBlank private String classId;
    @NotNull  private LocalDate sessionDate;
    @NotNull  private LocalTime startTime;
    @NotNull  private LocalTime endTime;
    private SessionType type;
    private Integer gracePeriodMinutes;
}
