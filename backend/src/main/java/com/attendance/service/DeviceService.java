package com.attendance.service;

import com.attendance.dto.request.DeviceRequest;
import com.attendance.dto.response.DeviceResponse;
import com.attendance.entity.Device;
import com.attendance.enums.DeviceStatus;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;

    public DeviceResponse registerDevice(DeviceRequest request) {
        String rawApiKey = UUID.randomUUID().toString().replace("-", "") +
                           UUID.randomUUID().toString().replace("-", "");
        String hashedKey = passwordEncoder.encode(rawApiKey);

        Device device = Device.builder()
                .name(request.getName())
                .location(request.getLocation())
                .apiKeyHash(hashedKey)
                .status(DeviceStatus.ACTIVE)
                .build();
        device = deviceRepository.save(device);

        DeviceResponse response = toResponse(device);
        response.setApiKey(rawApiKey);
        return response;
    }

    public List<DeviceResponse> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DeviceResponse getDevice(String id) {
        return toResponse(deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + id)));
    }

    public DeviceResponse updateDevice(String id, DeviceRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + id));
        device.setName(request.getName());
        device.setLocation(request.getLocation());
        return toResponse(deviceRepository.save(device));
    }

    public void deleteDevice(String id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found: " + id));
        device.setDeletedAt(java.time.LocalDateTime.now());
        deviceRepository.save(device);
    }

    private DeviceResponse toResponse(Device d) {
        return DeviceResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .location(d.getLocation())
                .status(d.getStatus())
                .lastHeartbeat(d.getLastHeartbeat())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
