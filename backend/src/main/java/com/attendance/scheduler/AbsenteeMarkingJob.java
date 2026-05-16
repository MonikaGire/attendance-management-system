package com.attendance.scheduler;

import com.attendance.entity.*;
import com.attendance.enums.AttendanceSource;
import com.attendance.enums.AttendanceStatus;
import com.attendance.repository.*;
import com.attendance.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbsenteeMarkingJob {

    private final SessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    @Value("${app.attendance.absentee-delay-minutes}")
    private int delayMinutes;

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void markAbsentees() {
        List<Session> sessions = sessionRepository.findSessionsReadyForAbsenteeMarking(delayMinutes);
        log.info("AbsenteeMarkingJob: processing {} sessions", sessions.size());

        for (Session session : sessions) {
            List<String> studentIds = enrollmentRepository.findStudentIdsByClassId(
                    session.getSchoolClass().getId());
            List<AttendanceRecord> existing = attendanceRepository.findBySessionId(session.getId());
            List<String> presentStudentIds = new ArrayList<>();
            for (AttendanceRecord r : existing) {
                if (r.getStudent() != null) presentStudentIds.add(r.getStudent().getId());
            }

            for (String studentId : studentIds) {
                if (!presentStudentIds.contains(studentId)) {
                    Student student = studentRepository.findById(studentId).orElse(null);
                    if (student == null) continue;

                    AttendanceRecord record = AttendanceRecord.builder()
                            .student(student)
                            .session(session)
                            .status(AttendanceStatus.ABSENT)
                            .source(AttendanceSource.MANUAL)
                            .build();
                    record = attendanceRepository.save(record);
                    notificationService.sendAttendanceNotification(record.getId());
                }
            }

            session.setStatus("PROCESSED");
            sessionRepository.save(session);
        }
    }
}
