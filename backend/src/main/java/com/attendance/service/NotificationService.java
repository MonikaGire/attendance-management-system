package com.attendance.service;

import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.Student;
import com.attendance.enums.AttendanceStatus;
import com.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WhatsAppService whatsAppService;
    private final AttendanceRepository attendanceRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Async("notificationExecutor")
    @Transactional(readOnly = true)
    public void sendAttendanceNotification(String recordId) {
        AttendanceRecord record = attendanceRepository.findById(recordId).orElse(null);
        if (record == null) return;

        Student student = record.getStudent();
        if (student == null) return;

        String name = student.getFirstName() + " " + student.getLastName();
        String className = record.getSession().getSchoolClass().getName();
        String date = record.getSession().getSessionDate().format(DATE_FMT);
        String time = record.getCheckInTime() != null
                ? record.getCheckInTime().format(TIME_FMT) : "N/A";

        if (record.getStatus() == AttendanceStatus.PRESENT) {
            if (Boolean.TRUE.equals(student.getWhatsappConsent()) && student.getPhone() != null) {
                whatsAppService.sendPresent(student.getPhone(), name, className, date, time, recordId);
            }
        } else if (record.getStatus() == AttendanceStatus.LATE) {
            if (Boolean.TRUE.equals(student.getWhatsappConsent()) && student.getPhone() != null) {
                whatsAppService.sendLate(student.getPhone(), name, className, date, time, recordId);
            }
        } else if (record.getStatus() == AttendanceStatus.ABSENT) {
            if (Boolean.TRUE.equals(student.getWhatsappConsent()) && student.getParentPhone() != null) {
                whatsAppService.sendAbsent(student.getParentPhone(), name, className, date, recordId);
            }
        }
    }
}
