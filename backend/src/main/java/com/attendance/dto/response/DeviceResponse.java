package com.attendance.dto.response;

import com.attendance.enums.DeviceStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {
    private String id;
    private String name;
    private String location;
    private DeviceStatus status;
    private LocalDateTime lastHeartbeat;
    private LocalDateTime createdAt;
    private String apiKey;
}
