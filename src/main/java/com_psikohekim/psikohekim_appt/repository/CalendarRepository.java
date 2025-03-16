package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CalendarRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByUserId(Long userId);
    List<CalendarEvent> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
    Optional<CalendarEvent> findByIdAndUserId(Long id, Long userId);
    Optional<CalendarEvent> findByExternalIdAndSource(String externalId, String source);
    List<CalendarEvent> findByUserIdAndStartTimeAfterOrderByStartTime(Long userId, LocalDateTime time);
    List<CalendarEvent> findByUserIdAndTitleContainingIgnoreCase(Long userId, String keyword);
    boolean existsByUserIdAndStartTimeLessThanAndEndTimeGreaterThan(Long userId, LocalDateTime end, LocalDateTime start);
    long countByUserId(Long userId);
    long countByUserIdAndStartTimeAfter(Long userId, LocalDateTime time);
    long countByUserIdAndSource(Long userId, String source);
    @Query("SELECT MAX(e.updatedAt) FROM CalendarEvent e WHERE e.userId = :userId AND e.source = :source")
    LocalDateTime findLastSyncTimeByUserIdAndSource(Long userId, String source);

    @Modifying
    @Query("UPDATE CalendarEvent e SET e.updatedAt = :syncTime WHERE e.userId = :userId AND e.source = :source")
    void updateLastSyncTime(Long userId, String source, LocalDateTime syncTime);

    boolean existsByUserIdAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(Long userId, LocalDateTime end, LocalDateTime start, Long excludeEventId);

}
