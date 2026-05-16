package com.attendance.controller;

import com.attendance.dto.request.SessionRequest;
import com.attendance.dto.response.ApiResponse;
import com.attendance.entity.SchoolClass;
import com.attendance.entity.Session;
import com.attendance.enums.SessionType;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.SchoolClassRepository;
import com.attendance.repository.SessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions")
public class SessionController {

    private final SessionRepository sessionRepository;
    private final SchoolClassRepository classRepository;

    @GetMapping
    @Operation(summary = "Get sessions by class and date")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSessions(
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Session> sessions;
        if (classId != null && date != null) {
            sessions = sessionRepository.findBySchoolClassIdAndSessionDate(classId, date);
        } else {
            sessions = sessionRepository.findAll();
        }
        return ResponseEntity.ok(ApiResponse.success(sessions.stream().map(this::toMap).toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSession(@PathVariable String id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(toMap(session)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Create session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSession(
            @Valid @RequestBody SessionRequest request) {
        SchoolClass sc = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        Session session = Session.builder()
                .schoolClass(sc)
                .sessionDate(request.getSessionDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .type(request.getType() != null ? request.getType() : SessionType.REGULAR)
                .gracePeriodMinutes(request.getGracePeriodMinutes() != null
                        ? request.getGracePeriodMinutes() : 15)
                .status("SCHEDULED")
                .build();
        return ResponseEntity.ok(ApiResponse.success("Session created",
                toMap(sessionRepository.save(session))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Update session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSession(
            @PathVariable String id, @Valid @RequestBody SessionRequest request) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + id));
        session.setSessionDate(request.getSessionDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        if (request.getType() != null) session.setType(request.getType());
        if (request.getGracePeriodMinutes() != null)
            session.setGracePeriodMinutes(request.getGracePeriodMinutes());
        return ResponseEntity.ok(ApiResponse.success(toMap(sessionRepository.save(session))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete session")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable String id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + id));
        session.setDeletedAt(LocalDateTime.now());
        sessionRepository.save(session);
        return ResponseEntity.ok(ApiResponse.success("Session deleted", null));
    }

    private Map<String, Object> toMap(Session s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("classId", s.getSchoolClass() != null ? s.getSchoolClass().getId() : null);
        m.put("className", s.getSchoolClass() != null ? s.getSchoolClass().getName() : null);
        m.put("sessionDate", s.getSessionDate());
        m.put("startTime", s.getStartTime());
        m.put("endTime", s.getEndTime());
        m.put("type", s.getType());
        m.put("gracePeriodMinutes", s.getGracePeriodMinutes());
        m.put("status", s.getStatus());
        m.put("createdAt", s.getCreatedAt());
        return m;
    }
}
