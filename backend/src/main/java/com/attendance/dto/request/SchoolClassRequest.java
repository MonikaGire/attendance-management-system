package com.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SchoolClassRequest {
    @NotBlank private String name;
    private String teacherId;
    @NotBlank private String academicYear;
    private String room;
    private String scheduleJson;
}
