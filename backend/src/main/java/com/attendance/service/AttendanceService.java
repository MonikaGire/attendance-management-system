package com.attendance.service;

import com.attendance.dto.request.AttendanceOverrideRequest;
import com.attendance.dto.response.AttendanceResponse;
import com.attendance.dto.response.SessionAttendanceResponse;
import com.attendance.entity.*;
import com.attendance.enums.AttendanceSource;
import com.attendance.enums.AttendanceStatus;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public SessionAttendanceResponse getSessionAttendance(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        List<String> enrolledStudentIds = enrollmentRepository.findStudentIdsByClassId(
                session.getSchoolClass().getId());

        List<AttendanceRecord> records = attendanceRepository.findBySessionId(sessionId);
        Map<String, AttendanceRecord> recordMap = records.stream()
                .collect(Collectors.toMap(r -> r.getStudent().getId(), r -> r));

        List<AttendanceResponse> responseList = new ArrayList<>();
        for (String studentId : enrolledStudentIds) {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) continue;

            AttendanceRecord record = recordMap.get(studentId);
            if (record != null) {
                responseList.add(toResponse(record));
            } else {
                responseList.add(AttendanceResponse.builder()
                        .studentId(studentId)
                        .studentName(student.getFirstName() + " " + student.getLastName())
                        .biometricId(student.getBiometricId())
                        .sessionId(sessionId)
                        .sessionDate(session.getSessionDate())
                        .sessionStartTime(session.getStartTime())
                        .className(session.getSchoolClass().getName())
                        .status(AttendanceStatus.ABSENT)
                        .source(AttendanceSource.MANUAL)
                        .build());
            }
        }

        long present = responseList.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long late = responseList.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
        long absent = responseList.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();

        return SessionAttendanceResponse.builder()
                .sessionId(sessionId)
                .sessionDate(session.getSessionDate())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .classId(session.getSchoolClass().getId())
                .className(session.getSchoolClass().getName())
                .totalStudents(responseList.size())
                .presentCount((int) present)
                .lateCount((int) late)
                .absentCount((int) absent)
                .records(responseList)
                .build();
    }

    @Transactional
    public AttendanceResponse overrideAttendance(AttendanceOverrideRequest request, String overridingUserId) {
        AttendanceRecord record = attendanceRepository
                .findByStudentIdAndSessionId(request.getStudentId(), request.getSessionId())
                .orElseGet(() -> {
                    Student student = studentRepository.findById(request.getStudentId())
                            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
                    Session session = sessionRepository.findById(request.getSessionId())
                            .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
                    return AttendanceRecord.builder()
                            .student(student)
                            .session(session)
                            .source(AttendanceSource.OVERRIDE)
                            .build();
                });

        String oldStatus = record.getStatus() != null ? record.getStatus().name() : "NONE";
        record.setStatus(request.getNewStatus());
        record.setSource(AttendanceSource.OVERRIDE);
        record.setNotes(request.getNotes());
        record.setOverriddenBy(overridingUserId);
        record = attendanceRepository.save(record);

        auditService.log(overridingUserId, "OVERRIDE_ATTENDANCE", "attendance_records",
                record.getId(), oldStatus, request.getNewStatus().name(), null);
        notificationService.sendAttendanceNotification(record.getId());

        return toResponse(record);
    }

    public Page<AttendanceResponse> getStudentReport(String studentId, LocalDate from, LocalDate to, Pageable pageable) {
        return attendanceRepository.findByStudentIdAndDateRange(studentId, from, to, pageable)
                .map(this::toResponse);
    }

    public double calculateAttendanceRate(String studentId, String classId) {
        List<Session> sessions = sessionRepository.findByClassIdAndDateRange(
                classId, LocalDate.now().minusMonths(3), LocalDate.now());
        if (sessions.isEmpty()) return 0;
        long present = attendanceRepository.countPresentByStudentAndClass(studentId, classId);
        return (double) present / sessions.size() * 100;
    }

    public List<AttendanceResponse> getRecentEvents(int limit) {
        return attendanceRepository.findRecentByDate(LocalDate.now(),
                        PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AttendanceResponse toResponse(AttendanceRecord r) {
        return AttendanceResponse.builder()
                .id(r.getId())
                .studentId(r.getStudent() != null ? r.getStudent().getId() : null)
                .studentName(r.getStudent() != null
                        ? r.getStudent().getFirstName() + " " + r.getStudent().getLastName() : null)
                .biometricId(r.getStudent() != null ? r.getStudent().getBiometricId() : null)
                .sessionId(r.getSession() != null ? r.getSession().getId() : null)
                .sessionDate(r.getSession() != null ? r.getSession().getSessionDate() : null)
                .sessionStartTime(r.getSession() != null ? r.getSession().getStartTime() : null)
                .className(r.getSession() != null && r.getSession().getSchoolClass() != null
                        ? r.getSession().getSchoolClass().getName() : null)
                .status(r.getStatus())
                .checkInTime(r.getCheckInTime())
                .source(r.getSource())
                .deviceId(r.getDeviceId())
                .notes(r.getNotes())
                .overriddenBy(r.getOverriddenBy())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
