package com.attendance.repository;

import com.attendance.entity.AttendanceRecord;
import com.attendance.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, String> {

    Optional<AttendanceRecord> findByStudentIdAndSessionId(String studentId, String sessionId);

    List<AttendanceRecord> findBySessionId(String sessionId);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.student.id = :studentId " +
           "AND ar.session.sessionDate BETWEEN :from AND :to ORDER BY ar.session.sessionDate DESC")
    Page<AttendanceRecord> findByStudentIdAndDateRange(@Param("studentId") String studentId,
                                                        @Param("from") LocalDate from,
                                                        @Param("to") LocalDate to,
                                                        Pageable pageable);

    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student.id = :studentId " +
           "AND ar.session.schoolClass.id = :classId " +
           "AND ar.status IN ('PRESENT', 'LATE')")
    long countPresentByStudentAndClass(@Param("studentId") String studentId,
                                        @Param("classId") String classId);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.session.id IN :sessionIds " +
           "AND ar.student.id NOT IN :studentIds")
    List<AttendanceRecord> findMissingAttendance(@Param("sessionIds") List<String> sessionIds,
                                                  @Param("studentIds") List<String> studentIds);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.session.sessionDate = :date")
    List<AttendanceRecord> findBySessionDate(@Param("date") LocalDate date);

    @Query("SELECT ar.status, COUNT(ar) FROM AttendanceRecord ar " +
           "WHERE ar.session.sessionDate = :date GROUP BY ar.status")
    List<Object[]> countByStatusForDate(@Param("date") LocalDate date);

    @Query("SELECT ar.session.sessionDate, ar.status, COUNT(ar) FROM AttendanceRecord ar " +
           "WHERE ar.session.sessionDate BETWEEN :from AND :to " +
           "GROUP BY ar.session.sessionDate, ar.status ORDER BY ar.session.sessionDate")
    List<Object[]> getAttendanceTrend(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.session.schoolClass.id = :classId " +
           "AND ar.session.sessionDate BETWEEN :from AND :to ORDER BY ar.session.sessionDate DESC")
    Page<AttendanceRecord> findByClassIdAndDateRange(@Param("classId") String classId,
                                                      @Param("from") LocalDate from,
                                                      @Param("to") LocalDate to,
                                                      Pageable pageable);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.session.sessionDate = :date " +
           "ORDER BY ar.createdAt DESC")
    List<AttendanceRecord> findRecentByDate(@Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT ar.student.id, COUNT(ar) as absentCount FROM AttendanceRecord ar " +
           "WHERE ar.status = 'ABSENT' AND ar.session.schoolClass.id = :classId " +
           "AND ar.session.sessionDate BETWEEN :from AND :to " +
           "GROUP BY ar.student.id ORDER BY absentCount DESC")
    List<Object[]> findTopAbsentStudentsByClass(@Param("classId") String classId,
                                                 @Param("from") LocalDate from,
                                                 @Param("to") LocalDate to,
                                                 Pageable pageable);

    boolean existsByStudentIdAndSessionId(String studentId, String sessionId);

    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.session.schoolClass.id = :classId " +
           "AND ar.session.sessionDate = :date")
    List<AttendanceRecord> findByClassIdAndDate(@Param("classId") String classId,
                                                 @Param("date") LocalDate date);
}
