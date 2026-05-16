package com.attendance.controller;

import com.attendance.dto.response.ApiResponse;
import com.attendance.dto.response.AttendanceResponse;
import com.attendance.dto.response.DashboardSummaryResponse;
import com.attendance.entity.*;
import com.attendance.repository.*;
import com.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

    private final AttendanceRepository attendanceRepository;
    private final DeviceRepository deviceRepository;
    private final SchoolClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceService attendanceService;

    @GetMapping("/summary")
    @Operation(summary = "Today's dashboard summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        LocalDate today = LocalDate.now();
        List<Object[]> statusCounts = attendanceRepository.countByStatusForDate(today);

        long present = 0, absent = 0, late = 0;
        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            if ("PRESENT".equals(status)) present = count;
            else if ("ABSENT".equals(status)) absent = count;
            else if ("LATE".equals(status)) late = count;
        }
        long total = present + absent + late;
        double rate = total > 0 ? (double)(present + late) / total * 100 : 0;
        long activeDevices = deviceRepository.countActiveDevices();

        List<DashboardSummaryResponse.TrendPoint> trend = getTrend(7);
        List<DashboardSummaryResponse.ClassSummary> classSummaries = getClassSummaries(today);
        List<com.attendance.dto.response.AttendanceResponse> recentEvents = attendanceService.getRecentEvents(10);

        DashboardSummaryResponse response = DashboardSummaryResponse.builder()
                .date(today)
                .attendanceRate(Math.round(rate * 100.0) / 100.0)
                .totalPresent(present + late)
                .totalAbsent(absent)
                .totalLate(late)
                .activeDevices(activeDevices)
                .trend(trend)
                .classSummaries(classSummaries)
                .recentEvents(recentEvents)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/trend")
    @Operation(summary = "Attendance trend for N days")
    public ResponseEntity<ApiResponse<List<DashboardSummaryResponse.TrendPoint>>> getTrendData(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.success(getTrend(days)));
    }

    @GetMapping("/class-summary")
    @Operation(summary = "Per-class summary for today")
    public ResponseEntity<ApiResponse<List<DashboardSummaryResponse.ClassSummary>>> getClassSummary() {
        return ResponseEntity.ok(ApiResponse.success(getClassSummaries(LocalDate.now())));
    }

    private List<DashboardSummaryResponse.TrendPoint> getTrend(int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1);
        LocalDate to = LocalDate.now();
        List<Object[]> raw = attendanceRepository.getAttendanceTrend(from, to);

        Map<LocalDate, DashboardSummaryResponse.TrendPoint> map = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = from.plusDays(i);
            map.put(d, DashboardSummaryResponse.TrendPoint.builder()
                    .date(d).presentCount(0).absentCount(0).lateCount(0).build());
        }
        for (Object[] row : raw) {
            LocalDate date = (LocalDate) row[0];
            String status = (String) row[1];
            Long count = (Long) row[2];
            DashboardSummaryResponse.TrendPoint point = map.get(date);
            if (point != null) {
                if ("PRESENT".equals(status)) point.setPresentCount(count);
                else if ("ABSENT".equals(status)) point.setAbsentCount(count);
                else if ("LATE".equals(status)) point.setLateCount(count);
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<DashboardSummaryResponse.ClassSummary> getClassSummaries(LocalDate date) {
        List<SchoolClass> classes = classRepository.findAll();
        List<DashboardSummaryResponse.ClassSummary> summaries = new ArrayList<>();
        for (SchoolClass sc : classes) {
            List<String> studentIds = enrollmentRepository.findStudentIdsByClassId(sc.getId());
            List<AttendanceRecord> records = attendanceRepository.findByClassIdAndDate(sc.getId(), date);
            long present = records.stream().filter(r -> "PRESENT".equals(r.getStatus().name())).count();
            long late = records.stream().filter(r -> "LATE".equals(r.getStatus().name())).count();
            long absent = records.stream().filter(r -> "ABSENT".equals(r.getStatus().name())).count();
            long total = present + late + absent;
            double rate = total > 0 ? (double)(present + late) / total * 100 : 0;
            summaries.add(DashboardSummaryResponse.ClassSummary.builder()
                    .classId(sc.getId())
                    .className(sc.getName())
                    .totalStudents(studentIds.size())
                    .presentCount((int) present)
                    .lateCount((int) late)
                    .absentCount((int) absent)
                    .attendanceRate(Math.round(rate * 100.0) / 100.0)
                    .build());
        }
        return summaries;
    }
}
