package com.attendance.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String phone;
    private String parentPhone;
    private String grade;
    private String biometricId;
    private LocalDate enrollmentDate;
    private Boolean whatsappConsent;
    private String status;
    private LocalDateTime createdAt;
}
