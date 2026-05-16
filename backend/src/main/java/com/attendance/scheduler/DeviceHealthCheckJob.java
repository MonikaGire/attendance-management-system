package com.attendance.scheduler;

import com.attendance.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceHealthCheckJob {

    private final DeviceRepository deviceRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelay = 600000)
    public void checkDeviceHealth() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        int count = deviceRepository.markOfflineDevices(threshold);
        if (count > 0) {
            log.warn("DeviceHealthCheckJob: {} device(s) marked OFFLINE", count);
            messagingTemplate.convertAndSend("/topic/devices", "DEVICE_STATUS_CHANGED");
        }
    }
}
