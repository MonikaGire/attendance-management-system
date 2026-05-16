package com.attendance.service;

import com.attendance.dto.request.BiometricEventRequest;
import com.attendance.entity.*;
import com.attendance.enums.AttendanceSource;
import com.attendance.enums.AttendanceStatus;
import com.attendance.enums.DeviceStatus;
import com.attendance.exception.DeviceNotAuthorizedException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiometricService {

    private final DeviceRepository deviceRepository;
    private final DeviceEventRepository deviceEventRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.biometric.dedup-window-minutes}")
    private int dedupWindowMinutes;

    public Device validateDevice(String apiKey) {
        List<Device> devices = deviceRepository.findByStatus(DeviceStatus.ACTIVE);
        for (Device device : devices) {
            if (passwordEncoder.matches(apiKey, device.getApiKeyHash())) {
                device.setLastHeartbeat(LocalDateTime.now());
                deviceRepository.save(device);
                return device;
            }
        }
        throw new DeviceNotAuthorizedException("Invalid device API key");
    }

    @Transactional
    public DeviceEvent recordEvent(Device device, BiometricEventRequest request) {
        DeviceEvent event = DeviceEvent.builder()
                .device(device)
                .biometricId(request.getBiometricId())
                .rawTimestamp(request.getTimestamp())
                .processed(false)
                .build();
        return deviceEventRepository.save(event);
    }

    @Async("biometricExecutor")
    @Transactional
    public void processEventAsync(String eventId) {
        DeviceEvent event = deviceEventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        LocalDateTime ts = event.getRawTimestamp();
        long duplicates = deviceEventRepository.countDuplicateEvents(
                event.getBiometricId(),
                event.getDevice().getId(),
                ts.minusMinutes(dedupWindowMinutes),
                ts.plusMinutes(dedupWindowMinutes));

        if (duplicates > 0) {
            log.info("Duplicate biometric event skipped: {}", eventId);
            event.setProcessed(true);
            deviceEventRepository.save(event);
            return;
        }

        Student student = studentRepository.findByBiometricId(event.getBiometricId()).orElse(null);
        if (student == null) {
            log.warn("No student found for biometric ID: {}", event.getBiometricId());
            return;
        }

        List<String> classIds = enrollmentRepository.findClassIdsByStudentId(student.getId());
        if (classIds.isEmpty()) {
            log.warn("Student {} is not enrolled in any class", student.getId());
            return;
        }

        LocalDate eventDate = ts.toLocalDate();
        LocalTime eventTime = ts.toLocalTime();

        List<Session> activeSessions = sessionRepository.findActiveSessionsForClasses(
                classIds, eventDate, eventTime);

        Session targetSession = null;
        if (!activeSessions.isEmpty()) {
            targetSession = activeSessions.get(0);
        } else {
            List<Session> upcoming = sessionRepository.findUpcomingSessionsForClasses(
                    classIds, eventDate, eventTime, eventTime.plusMinutes(30));
            if (!upcoming.isEmpty()) {
                targetSession = upcoming.get(0);
            }
        }

        if (targetSession == null) {
            log.info("No active/upcoming session for student {} at {}", student.getId(), ts);
            return;
        }

        if (attendanceRepository.existsByStudentIdAndSessionId(student.getId(), targetSession.getId())) {
            log.info("Attendance already recorded for student {} session {}", student.getId(), targetSession.getId());
            event.setProcessed(true);
            deviceEventRepository.save(event);
            return;
        }

        AttendanceStatus status = determineStatus(eventTime, targetSession);

        AttendanceRecord record = AttendanceRecord.builder()
                .student(student)
                .session(targetSession)
                .status(status)
                .checkInTime(ts)
                .source(AttendanceSource.BIOMETRIC)
                .deviceId(event.getDevice().getId())
                .build();
        record = attendanceRepository.save(record);

        event.setProcessed(true);
        event.setAttendanceRecordId(record.getId());
        deviceEventRepository.save(event);

        notificationService.sendAttendanceNotification(record.getId());

        messagingTemplate.convertAndSend(
                "/topic/attendance/" + targetSession.getSchoolClass().getId(), record.getId());

        log.info("Processed biometric event: student={}, status={}", student.getId(), status);
    }

    private AttendanceStatus determineStatus(LocalTime checkInTime, Session session) {
        LocalTime deadline = session.getStartTime()
                .plusMinutes(session.getGracePeriodMinutes() != null ? session.getGracePeriodMinutes() : 15);
        return checkInTime.isBefore(deadline) || checkInTime.equals(deadline)
                ? AttendanceStatus.PRESENT : AttendanceStatus.LATE;
    }
}
