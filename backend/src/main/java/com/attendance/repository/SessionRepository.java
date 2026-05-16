package com.attendance.repository;

import com.attendance.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, String> {

    List<Session> findBySchoolClassIdAndSessionDate(String classId, LocalDate date);

    @Query("SELECT s FROM Session s WHERE s.schoolClass.id IN :classIds " +
           "AND s.sessionDate = :date " +
           "AND s.startTime <= :time AND s.endTime >= :time " +
           "AND s.deletedAt IS NULL")
    List<Session> findActiveSessionsForClasses(@Param("classIds") List<String> classIds,
                                               @Param("date") LocalDate date,
                                               @Param("time") LocalTime time);

    @Query("SELECT s FROM Session s WHERE s.schoolClass.id IN :classIds " +
           "AND s.sessionDate = :date " +
           "AND s.startTime > :time " +
           "AND s.startTime <= :upcomingTime " +
           "AND s.deletedAt IS NULL")
    List<Session> findUpcomingSessionsForClasses(@Param("classIds") List<String> classIds,
                                                  @Param("date") LocalDate date,
                                                  @Param("time") LocalTime time,
                                                  @Param("upcomingTime") LocalTime upcomingTime);

    @Query(value = "SELECT s.* FROM sessions s " +
                   "WHERE TIMESTAMP(s.session_date, s.end_time) < DATE_SUB(NOW(), INTERVAL :delayMinutes MINUTE) " +
                   "AND s.status != 'PROCESSED' AND s.deleted_at IS NULL",
           nativeQuery = true)
    List<Session> findSessionsReadyForAbsenteeMarking(@Param("delayMinutes") int delayMinutes);

    @Query("SELECT s FROM Session s WHERE s.schoolClass.id = :classId " +
           "AND s.sessionDate BETWEEN :from AND :to")
    List<Session> findByClassIdAndDateRange(@Param("classId") String classId,
                                             @Param("from") LocalDate from,
                                             @Param("to") LocalDate to);
}
