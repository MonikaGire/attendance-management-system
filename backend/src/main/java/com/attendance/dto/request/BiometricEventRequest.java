package com.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BiometricEventRequest {
    @NotBlank
    private String biometricId;
    @NotNull
    private LocalDateTime timestamp;
}
