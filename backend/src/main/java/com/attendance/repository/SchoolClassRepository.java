package com.attendance.repository;

import com.attendance.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, String> {
    List<SchoolClass> findByTeacherId(String teacherId);

    @Query("SELECT sc FROM SchoolClass sc WHERE sc.academicYear = :year")
    List<SchoolClass> findByAcademicYear(@Param("year") String academicYear);
}
