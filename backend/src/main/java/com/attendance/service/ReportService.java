package com.attendance.service;

import com.attendance.dto.response.AttendanceResponse;
import com.attendance.dto.response.ReportResponse;
import com.attendance.entity.*;
import com.attendance.repository.*;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;
    private final AttendanceService attendanceService;

    public ReportResponse getClassReport(String classId, LocalDate from, LocalDate to, int page, int size) {
        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new com.attendance.exception.ResourceNotFoundException("Class not found: " + classId));

        List<Session> sessions = sessionRepository.findByClassIdAndDateRange(classId, from, to);

        Page<AttendanceRecord> records = attendanceRepository.findByClassIdAndDateRange(
                classId, from, to, PageRequest.of(page, size, Sort.by("session.sessionDate").descending()));

        long present = records.getContent().stream()
                .filter(r -> r.getStatus().name().equals("PRESENT")).count();
        long late = records.getContent().stream()
                .filter(r -> r.getStatus().name().equals("LATE")).count();
        long absent = records.getContent().stream()
                .filter(r -> r.getStatus().name().equals("ABSENT")).count();

        long total = present + late + absent;
        double presentPct = total > 0 ? (double)(present + late) / total * 100 : 0;

        List<Object[]> topAbsent = attendanceRepository.findTopAbsentStudentsByClass(
                classId, from, to, PageRequest.of(0, 5));

        List<ReportResponse.TopAbsentStudent> topAbsentStudents = topAbsent.stream().map(row -> {
            String studentId = (String) row[0];
            Long count = (Long) row[1];
            Student s = studentRepository.findById(studentId).orElse(null);
            String name = s != null ? s.getFirstName() + " " + s.getLastName() : studentId;
            return ReportResponse.TopAbsentStudent.builder()
                    .studentId(studentId)
                    .studentName(name)
                    .absentCount(count)
                    .build();
        }).collect(Collectors.toList());

        return ReportResponse.builder()
                .from(from)
                .to(to)
                .classId(classId)
                .className(schoolClass.getName())
                .totalSessions(sessions.size())
                .presentPercent(Math.round(presentPct * 100.0) / 100.0)
                .presentCount(present + late)
                .absentCount(absent)
                .lateCount(late)
                .topAbsentStudents(topAbsentStudents)
                .records(records.getContent().stream()
                        .map(attendanceService::toResponse).collect(Collectors.toList()))
                .build();
    }

    public byte[] exportCsv(String classId, LocalDate from, LocalDate to) throws IOException {
        List<AttendanceRecord> records = attendanceRepository.findByClassIdAndDateRange(
                classId, from, to, Pageable.unpaged()).getContent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {
            writer.writeNext(new String[]{"Student Name", "Biometric ID", "Class", "Date",
                    "Status", "Check-in Time", "Source"});

            DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (AttendanceRecord r : records) {
                writer.writeNext(new String[]{
                        r.getStudent().getFirstName() + " " + r.getStudent().getLastName(),
                        r.getStudent().getBiometricId(),
                        r.getSession().getSchoolClass().getName(),
                        r.getSession().getSessionDate().toString(),
                        r.getStatus().name(),
                        r.getCheckInTime() != null ? r.getCheckInTime().format(dtFmt) : "",
                        r.getSource().name()
                });
            }
        }
        return out.toByteArray();
    }

    public ReportResponse getDailyReport(LocalDate date) {
        List<Object[]> statusCounts = attendanceRepository.countByStatusForDate(date);
        long present = 0, absent = 0, late = 0;
        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            if ("PRESENT".equals(status)) present = count;
            else if ("ABSENT".equals(status)) absent = count;
            else if ("LATE".equals(status)) late = count;
        }
        long total = present + absent + late;
        double pct = total > 0 ? (double)(present + late) / total * 100 : 0;

        return ReportResponse.builder()
                .from(date)
                .to(date)
                .presentCount(present + late)
                .absentCount(absent)
                .lateCount(late)
                .presentPercent(Math.round(pct * 100.0) / 100.0)
                .build();
    }
}
