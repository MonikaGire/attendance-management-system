package com.attendance.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionAttendanceResponse {
    private String sessionId;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String classId;
    private String className;
    private int totalStudents;
    private int presentCount;
    private int lateCount;
    private int absentCount;
    private List<AttendanceResponse> records;
}
