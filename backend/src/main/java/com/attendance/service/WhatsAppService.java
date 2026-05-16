package com.attendance.service;

import com.attendance.entity.Message;
import com.attendance.enums.MessageStatus;
import com.attendance.repository.MessageRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final MessageRepository messageRepository;

    @Value("${app.twilio.account-sid}")
    private String accountSid;

    @Value("${app.twilio.auth-token}")
    private String authToken;

    @Value("${app.twilio.whatsapp-from}")
    private String fromNumber;

    @Value("${app.twilio.enabled}")
    private boolean twilioEnabled;

    @Value("${app.school-name}")
    private String schoolName;

    @PostConstruct
    public void init() {
        if (twilioEnabled && accountSid != null && !accountSid.isBlank()) {
            Twilio.init(accountSid, authToken);
        }
    }

    public void sendPresent(String phone, String name, String className, String date, String time,
                             String attendanceRecordId) {
        String body = String.format(
                "Hi %s, attendance for %s on %s at %s recorded. – %s",
                name, className, date, time, schoolName);
        queueAndSend(phone, "attendance_present", body, attendanceRecordId, "STUDENT");
    }

    public void sendLate(String phone, String name, String className, String date, String time,
                          String attendanceRecordId) {
        String body = String.format(
                "Hi %s, you were marked LATE for %s on %s. Check-in: %s. – %s",
                name, className, date, time, schoolName);
        queueAndSend(phone, "attendance_late", body, attendanceRecordId, "STUDENT");
    }

    public void sendAbsent(String phone, String name, String className, String date,
                            String attendanceRecordId) {
        String body = String.format(
                "Alert: %s was absent from %s on %s. Contact school if incorrect. – %s",
                name, className, date, schoolName);
        queueAndSend(phone, "attendance_absent", body, attendanceRecordId, "PARENT");
    }

    public void sendDailySummary(String phone, String date, int sessions,
                                  String presentPct, int absent, int late) {
        String body = String.format(
                "Daily Report %s: Sessions=%d, Present=%s%%, Absent=%d, Late=%d – Attendance System",
                date, sessions, presentPct, absent, late);
        queueAndSend(phone, "daily_summary", body, null, "ADMIN");
    }

    private void queueAndSend(String phone, String template, String body,
                               String attendanceRecordId, String recipientType) {
        Message msg = Message.builder()
                .attendanceRecordId(attendanceRecordId)
                .recipientType(recipientType)
                .phone(phone)
                .templateName(template)
                .payload(body)
                .status(MessageStatus.QUEUED)
                .attempts(0)
                .build();
        msg = messageRepository.save(msg);
        attemptSend(msg, body);
    }

    private void attemptSend(Message msg, String body) {
        msg.setAttempts(msg.getAttempts() + 1);
        try {
            if (!twilioEnabled) {
                log.info("[WHATSAPP-MOCK] To: {} | {}", msg.getPhone(), body);
                msg.setStatus(MessageStatus.SENT);
                msg.setSentAt(LocalDateTime.now());
            } else {
                com.twilio.rest.api.v2010.account.Message twilioMsg =
                        com.twilio.rest.api.v2010.account.Message.creator(
                                new com.twilio.type.PhoneNumber("whatsapp:" + msg.getPhone()),
                                new com.twilio.type.PhoneNumber(fromNumber),
                                body).create();
                msg.setStatus(MessageStatus.SENT);
                msg.setSentAt(LocalDateTime.now());
                log.info("WhatsApp sent: SID={}", twilioMsg.getSid());
            }
        } catch (Exception e) {
            log.error("WhatsApp send failed: {}", e.getMessage());
            msg.setStatus(MessageStatus.FAILED);
            msg.setErrorMessage(e.getMessage());
        }
        messageRepository.save(msg);
    }

    @Scheduled(fixedDelay = 60000)
    public void retryFailedMessages() {
        List<Message> failed = messageRepository.findFailedMessagesForRetry(3);
        for (Message msg : failed) {
            log.info("Retrying message: {}", msg.getId());
            attemptSend(msg, msg.getPayload());
        }
    }
}
