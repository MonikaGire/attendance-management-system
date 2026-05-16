package com.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class StudentRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    private String phone;
    private String parentPhone;
    private String grade;
    @NotBlank private String biometricId;
    @NotNull  private LocalDate enrollmentDate;
    private Boolean whatsappConsent;
    private String classId;
}
