package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.CalendarEvent;
import com_psikohekim.psikohekim_appt.model.Therapist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface CalendarRepository extends JpaRepository<CalendarEvent, Long> {

    // Temel sorgular
    List<CalendarEvent> findByTherapist(Therapist therapist);
    List<CalendarEvent> findByTherapistAndStartTimeBetween(Therapist therapist, LocalDateTime start, LocalDateTime end);
    Optional<CalendarEvent> findByIdAndTherapist(Long id, Therapist therapist);

    // İstatistikler
    long countByTherapist(Therapist therapist);
    long countByTherapistAndStartTimeAfter(Therapist therapist, LocalDateTime time);
    long countByTherapistAndSource(Therapist therapist, String source);

    // Çakışma kontrolü
    boolean existsByTherapistAndStartTimeLessThanAndEndTimeGreaterThan(
            Therapist therapist,
            LocalDateTime end,
            LocalDateTime start
    );

    // Diğer sorgular
    Optional<CalendarEvent> findByExternalIdAndSource(String externalId, String source);
}