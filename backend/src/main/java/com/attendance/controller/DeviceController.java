package com.attendance.controller;

import com.attendance.dto.request.DeviceRequest;
import com.attendance.dto.response.ApiResponse;
import com.attendance.dto.response.DeviceResponse;
import com.attendance.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "Devices")
@PreAuthorize("hasRole('ADMIN')")
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @Operation(summary = "List all devices")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getAllDevices() {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getAllDevices()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDevice(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getDevice(id)));
    }

    @PostMapping
    @Operation(summary = "Register new device (API key shown once)")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Device registered. Store the API key securely.",
                deviceService.registerDevice(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update device")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDevice(
            @PathVariable String id, @Valid @RequestBody DeviceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.updateDevice(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete device")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable String id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.success("Device deleted", null));
    }
}
