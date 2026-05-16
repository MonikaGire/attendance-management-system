package com.attendance.controller;

import com.attendance.dto.request.SchoolClassRequest;
import com.attendance.dto.response.ApiResponse;
import com.attendance.entity.SchoolClass;
import com.attendance.entity.User;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.SchoolClassRepository;
import com.attendance.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
@Tag(name = "Classes")
public class SchoolClassController {

    private final SchoolClassRepository classRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List all classes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllClasses() {
        List<SchoolClass> classes = classRepository.findAll();
        List<Map<String, Object>> result = classes.stream().map(this::toMap).toList();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get class by ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getClass(@PathVariable String id) {
        SchoolClass sc = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(toMap(sc)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create class")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createClass(
            @Valid @RequestBody SchoolClassRequest request) {
        User teacher = null;
        if (request.getTeacherId() != null) {
            teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        }
        SchoolClass sc = SchoolClass.builder()
                .name(request.getName())
                .teacher(teacher)
                .academicYear(request.getAcademicYear())
                .room(request.getRoom())
                .scheduleJson(request.getScheduleJson())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Class created", toMap(classRepository.save(sc))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update class")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateClass(
            @PathVariable String id, @Valid @RequestBody SchoolClassRequest request) {
        SchoolClass sc = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + id));
        sc.setName(request.getName());
        sc.setAcademicYear(request.getAcademicYear());
        sc.setRoom(request.getRoom());
        sc.setScheduleJson(request.getScheduleJson());
        if (request.getTeacherId() != null) {
            sc.setTeacher(userRepository.findById(request.getTeacherId()).orElse(null));
        }
        return ResponseEntity.ok(ApiResponse.success(toMap(classRepository.save(sc))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete class")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable String id) {
        SchoolClass sc = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found: " + id));
        sc.setDeletedAt(LocalDateTime.now());
        classRepository.save(sc);
        return ResponseEntity.ok(ApiResponse.success("Class deleted", null));
    }

    private Map<String, Object> toMap(SchoolClass sc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", sc.getId());
        m.put("name", sc.getName());
        m.put("academicYear", sc.getAcademicYear());
        m.put("room", sc.getRoom());
        m.put("scheduleJson", sc.getScheduleJson());
        m.put("teacherId", sc.getTeacher() != null ? sc.getTeacher().getId() : null);
        m.put("teacherName", sc.getTeacher() != null
                ? sc.getTeacher().getFirstName() + " " + sc.getTeacher().getLastName() : null);
        m.put("createdAt", sc.getCreatedAt());
        return m;
    }
}
