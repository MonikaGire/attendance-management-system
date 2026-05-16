package com.attendance.repository;

import com.attendance.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    List<Enrollment> findByStudentId(String studentId);

    List<Enrollment> findBySchoolClassId(String classId);

    boolean existsByStudentIdAndSchoolClassId(String studentId, String classId);

    @Query("SELECT e.schoolClass.id FROM Enrollment e WHERE e.student.id = :studentId")
    List<String> findClassIdsByStudentId(@Param("studentId") String studentId);

    @Query("SELECT e.student.id FROM Enrollment e WHERE e.schoolClass.id = :classId")
    List<String> findStudentIdsByClassId(@Param("classId") String classId);

    void deleteByStudentIdAndSchoolClassId(String studentId, String classId);
}
