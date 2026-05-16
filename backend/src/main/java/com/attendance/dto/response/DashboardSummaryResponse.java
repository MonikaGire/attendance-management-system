package com.attendance.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private LocalDate date;
    private double attendanceRate;
    private long totalPresent;
    private long totalAbsent;
    private long totalLate;
    private long activeDevices;
    private List<TrendPoint> trend;
    private List<ClassSummary> classSummaries;
    private List<AttendanceResponse> recentEvents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private LocalDate date;
        private long presentCount;
        private long absentCount;
        private long lateCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassSummary {
        private String classId;
        private String className;
        private int totalStudents;
        private int presentCount;
        private int absentCount;
        private int lateCount;
        private double attendanceRate;
    }
}
