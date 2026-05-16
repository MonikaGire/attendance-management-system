package com.attendance.repository;

import com.attendance.entity.Message;
import com.attendance.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("SELECT m FROM Message m WHERE m.status = 'FAILED' AND m.attempts < :maxAttempts")
    List<Message> findFailedMessagesForRetry(@Param("maxAttempts") int maxAttempts);

    List<Message> findByStatus(MessageStatus status);
}
