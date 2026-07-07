package com.hungphu.crm.features.calendar.repository;

import com.hungphu.crm.features.calendar.entity.PersonalEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonalEventRepository extends JpaRepository<PersonalEvent, UUID> {

    // Lấy events trong khoảng thời gian (cho calendar view)
    @Query("""
        SELECT e FROM PersonalEvent e
        WHERE e.user.id = :userId
          AND e.eventDate BETWEEN :from AND :to
        ORDER BY e.eventDate ASC, e.startTime ASC NULLS LAST
        """)
    List<PersonalEvent> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // Lấy events theo ngày cụ thể
    List<PersonalEvent> findByUserIdAndEventDateOrderByStartTimeAsc(
            UUID userId, LocalDate eventDate);

    // Lấy 1 event (kiểm tra ownership)
    Optional<PersonalEvent> findByIdAndUserId(UUID id, UUID userId);

    // Đếm events hôm nay
    long countByUserIdAndEventDate(UUID userId, LocalDate eventDate);
}