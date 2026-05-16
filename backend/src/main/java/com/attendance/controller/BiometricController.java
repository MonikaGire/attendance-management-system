package com.attendance.controller;

import com.attendance.dto.request.BiometricEventRequest;
import com.attendance.dto.response.ApiResponse;
import com.attendance.entity.Device;
import com.attendance.entity.DeviceEvent;
import com.attendance.service.BiometricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/biometric")
@RequiredArgsConstructor
@Tag(name = "Biometric")
public class BiometricController {

    private final BiometricService biometricService;

    @PostMapping("/events")
    @Operation(summary = "Submit biometric scan event from device")
    public ResponseEntity<ApiResponse<Map<String, String>>> submitEvent(
            @RequestHeader("X-Device-API-Key") String apiKey,
            @Valid @RequestBody BiometricEventRequest request) {

        Device device = biometricService.validateDevice(apiKey);
        DeviceEvent event = biometricService.recordEvent(device, request);
        biometricService.processEventAsync(event.getId());

        return ResponseEntity.ok(ApiResponse.success("Event accepted",
                Map.of("eventId", event.getId())));
    }
}
