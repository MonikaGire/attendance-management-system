package com.attendance.controller;

import com.attendance.dto.request.AttendanceOverrideRequest;
import com.attendance.dto.response.ApiResponse;
import com.attendance.dto.response.AttendanceResponse;
import com.attendance.dto.response.SessionAttendanceResponse;
import com.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get attendance for a session")
    public ResponseEntity<ApiResponse<SessionAttendanceResponse>> getSessionAttendance(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getSessionAttendance(sessionId)));
    }

    @PostMapping("/override")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Override attendance record")
    public ResponseEntity<ApiResponse<AttendanceResponse>> overrideAttendance(
            @Valid @RequestBody AttendanceOverrideRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.overrideAttendance(request, userId)));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get student attendance report")
    public ResponseEntity<ApiResponse<Page<AttendanceResponse>>> getStudentReport(
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getStudentReport(studentId, from, to,
                        PageRequest.of(page, size, Sort.by("session.sessionDate").descending()))));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent attendance events")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getRecent(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getRecentEvents(limit)));
    }
}
