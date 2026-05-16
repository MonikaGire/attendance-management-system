package com.attendance.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private LocalDate from;
    private LocalDate to;
    private String classId;
    private String className;
    private int totalSessions;
    private double presentPercent;
    private long absentCount;
    private long lateCount;
    private long presentCount;
    private List<TopAbsentStudent> topAbsentStudents;
    private List<AttendanceResponse> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopAbsentStudent {
        private String studentId;
        private String studentName;
        private long absentCount;
    }
}
