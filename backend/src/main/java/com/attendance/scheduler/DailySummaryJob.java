package com.attendance.scheduler;

import com.attendance.entity.User;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.SessionRepository;
import com.attendance.repository.UserRepository;
import com.attendance.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailySummaryJob {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final WhatsAppService whatsAppService;

    @Scheduled(cron = "0 0 7 * * *")
    public void sendDailySummary() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));

        List<Object[]> statusCounts = attendanceRepository.countByStatusForDate(yesterday);
        long present = 0, absent = 0, late = 0;
        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            if ("PRESENT".equals(status)) present = count;
            else if ("ABSENT".equals(status)) absent = count;
            else if ("LATE".equals(status)) late = count;
        }

        long total = present + absent + late;
        int sessions = 1;
        String presentPct = total > 0
                ? String.format("%.1f", (double)(present + late) / total * 100)
                : "0.0";

        List<User> admins = userRepository.findByRoleName("ADMIN");
        for (User admin : admins) {
            if (admin.getPhone() != null) {
                whatsAppService.sendDailySummary(admin.getPhone(), dateStr, sessions,
                        presentPct, (int) absent, (int) late);
            }
        }
        log.info("DailySummaryJob: summary sent to {} admins", admins.size());
    }
}
