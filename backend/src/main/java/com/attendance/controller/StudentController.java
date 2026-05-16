package com.attendance.controller;

import com.attendance.dto.request.StudentRequest;
import com.attendance.dto.response.ApiResponse;
import com.attendance.dto.response.StudentResponse;
import com.attendance.entity.Student;
import com.attendance.entity.Enrollment;
import com.attendance.entity.SchoolClass;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Tag(name = "Students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SchoolClassRepository classRepository;

    @GetMapping
    @Operation(summary = "List/search students")
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> getStudents(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Student> students = search.isBlank()
                ? studentRepository.findAll(PageRequest.of(page, size))
                : studentRepository.searchStudents(search, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(students.map(this::toResponse)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudent(@PathVariable String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        return ResponseEntity.ok(ApiResponse.success(toResponse(student)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Create student")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody StudentRequest request) {
        Student student = Student.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .parentPhone(request.getParentPhone())
                .grade(request.getGrade())
                .biometricId(request.getBiometricId())
                .enrollmentDate(request.getEnrollmentDate())
                .whatsappConsent(request.getWhatsappConsent())
                .status("ACTIVE")
                .build();
        student = studentRepository.save(student);

        if (request.getClassId() != null) {
            SchoolClass schoolClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
            enrollmentRepository.save(Enrollment.builder()
                    .student(student).schoolClass(schoolClass).build());
        }
        return ResponseEntity.ok(ApiResponse.success("Student created", toResponse(student)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Update student")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable String id, @Valid @RequestBody StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setPhone(request.getPhone());
        student.setParentPhone(request.getParentPhone());
        student.setGrade(request.getGrade());
        student.setBiometricId(request.getBiometricId());
        student.setEnrollmentDate(request.getEnrollmentDate());
        student.setWhatsappConsent(request.getWhatsappConsent());
        return ResponseEntity.ok(ApiResponse.success(toResponse(studentRepository.save(student))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete student")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable String id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        student.setDeletedAt(LocalDateTime.now());
        studentRepository.save(student);
        return ResponseEntity.ok(ApiResponse.success("Student deleted", null));
    }

    private StudentResponse toResponse(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .phone(s.getPhone())
                .parentPhone(s.getParentPhone())
                .grade(s.getGrade())
                .biometricId(s.getBiometricId())
                .enrollmentDate(s.getEnrollmentDate())
                .whatsappConsent(s.getWhatsappConsent())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
