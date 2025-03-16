package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.request.CreateCalendarEventRequest;
import com_psikohekim.psikohekim_appt.dto.request.UpdateCalendarEventRequest;
import com_psikohekim.psikohekim_appt.dto.response.CalendarEventResponse;
import com_psikohekim.psikohekim_appt.dto.response.CalendarEventsResponse;
import com_psikohekim.psikohekim_appt.dto.response.CalendarSyncResponse;
import com_psikohekim.psikohekim_appt.enums.SyncStatus;
import com_psikohekim.psikohekim_appt.exception.CustomExceptionHandler;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.mapper.CalendarMapper;
import com_psikohekim.psikohekim_appt.model.CalendarEvent;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.repository.CalendarRepository;
import com_psikohekim.psikohekim_appt.repository.TherapistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarService {
    private final CalendarRepository eventRepository;
    private final CalendarMapper calendarMapper;
    private final TherapistRepository therapistRepository;

    public CalendarEventsResponse getEvents(Long therapistId, LocalDateTime start, LocalDateTime end) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found: " + therapistId));

        List<CalendarEvent> events;
        if (start != null && end != null) {
            events = eventRepository.findByTherapistAndStartTimeBetween(therapist, start, end);
        } else {
            events = eventRepository.findByTherapist(therapist);
        }

        Map<String, Long> stats = getEventStatistics(therapist);
        return new CalendarEventsResponse(calendarMapper.toResponseList(events), events.size(), stats);
    }

    public CalendarEventResponse createEvent(CreateCalendarEventRequest request, Long therapistId) {
        validateEventTimes(request.getStartTime(), request.getEndTime());

        if (hasTimeConflict(therapistId, request.getStartTime(), request.getEndTime())) {
            throw new CustomExceptionHandler.InvalidEventTimeException("Bu zaman aralığında başka bir etkinlik var");
        }

        CalendarEvent event = calendarMapper.toEntity(request);
        event.setTherapist(therapistRepository.getReferenceById(therapistId));
        event.setSource("MANUAL");
        event.setStatus("CONFIRMED");

        return CalendarEventResponse.fromEntity(eventRepository.save(event));
    }

    public CalendarEventResponse updateEvent(Long eventId, UpdateCalendarEventRequest request, Long therapistId) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found: " + therapistId));

        CalendarEvent existingEvent = eventRepository.findByIdAndTherapist(eventId, therapist)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        if (request.getStartTime() != null && request.getEndTime() != null) {
            validateEventTimes(request.getStartTime(), request.getEndTime());

            if (existsByTherapistAndStartTimeLessThanAndEndTimeGreaterThan(
                    therapist,
                    request.getStartTime(),
                    request.getEndTime())) {
                throw new CustomExceptionHandler.InvalidEventTimeException("Bu zaman aralığında başka bir etkinlik var");
            }
        }

        calendarMapper.updateEntityFromRequest(existingEvent, request);
        return CalendarEventResponse.fromEntity(eventRepository.save(existingEvent));
    }

    private boolean existsByTherapistAndStartTimeLessThanAndEndTimeGreaterThan(
            Therapist therapist,
            LocalDateTime start,
            LocalDateTime end) {
        return eventRepository.existsByTherapistAndStartTimeLessThanAndEndTimeGreaterThan(
                therapist, end, start);
    }

    public void deleteEvent(Long eventId, Long therapistId) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found: " + therapistId));

        CalendarEvent event = eventRepository.findByIdAndTherapist(eventId, therapist)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        eventRepository.delete(event);
    }

    private Map<String, Long> getEventStatistics(Therapist therapist) {
        Map<String, Long> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        stats.put("total", eventRepository.countByTherapist(therapist));
        stats.put("upcoming", eventRepository.countByTherapistAndStartTimeAfter(therapist, now));
        stats.put("google", eventRepository.countByTherapistAndSource(therapist, "GOOGLE"));
        stats.put("outlook", eventRepository.countByTherapistAndSource(therapist, "OUTLOOK"));
        stats.put("manual", eventRepository.countByTherapistAndSource(therapist, "MANUAL"));

        return stats;
    }

    public CalendarSyncResponse syncEvents(List<CalendarEvent> events) {
        try {
            int addedCount = 0;
            int updatedCount = 0;
            List<CalendarEvent> syncedEvents = new ArrayList<>();

            for (CalendarEvent event : events) {
                // Mevcut etkinliği bul veya yeni oluştur
                CalendarEvent existingEvent = eventRepository
                        .findByExternalIdAndSource(event.getExternalId(), event.getSource())
                        .orElse(null);

                if (existingEvent != null) {
                    // Güncelleme
                    updateExistingEvent(existingEvent, event);
                    syncedEvents.add(eventRepository.save(existingEvent));
                    updatedCount++;
                } else {
                    // Yeni ekle
                    syncedEvents.add(eventRepository.save(event));
                    addedCount++;
                }
            }

            return buildSuccessResponse(syncedEvents, addedCount, updatedCount);

        } catch (Exception e) {
            return buildErrorResponse(e.getMessage());
        }
    }

    // Yardımcı metodlar
    private void updateExistingEvent(CalendarEvent existingEvent, CalendarEvent newEvent) {
        existingEvent.setTitle(newEvent.getTitle());
        existingEvent.setDescription(newEvent.getDescription());
        existingEvent.setStartTime(newEvent.getStartTime());
        existingEvent.setEndTime(newEvent.getEndTime());
        existingEvent.setLocation(newEvent.getLocation());
        existingEvent.setStatus(newEvent.getStatus());
    }

    private CalendarSyncResponse buildSuccessResponse(List<CalendarEvent> syncedEvents,
                                                      int addedCount,
                                                      int updatedCount) {
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("added", (long) addedCount);
        statistics.put("updated", (long) updatedCount);
        statistics.put("total", (long) syncedEvents.size());
        statistics.put("lastSyncTime", System.currentTimeMillis());

        return CalendarSyncResponse.builder()
                .success(true)
                .events(calendarMapper.toResponseList(syncedEvents))
                .statistics(statistics)
                .syncStatus(SyncStatus.COMPLETED)
                .build();
    }

    public CalendarSyncResponse buildErrorResponse(String errorMessage) {
        return CalendarSyncResponse.builder()
                .success(false)
                .error("Takvim senkronizasyonu başarısız: " + errorMessage)
                .syncStatus(SyncStatus.FAILED)
                .build();
    }

    // CalendarService'de debug için
    private void validateEventTimes(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Server time: " + now);
        System.out.println("Event start time: " + start);

        if (start.isAfter(end)) {
            throw new CustomExceptionHandler.InvalidEventTimeException(
                    "Başlangıç zamanı bitiş zamanından sonra olamaz"
            );
        }

        if (start.isBefore(now)) {
            throw new CustomExceptionHandler.InvalidEventTimeException(
                    "Geçmiş bir zamana etkinlik eklenemez (" + start + " < " + now + ")"
            );
        }
    }

    private boolean hasTimeConflict(Long therapistId, LocalDateTime start, LocalDateTime end) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Therapist not found: " + therapistId));

        return eventRepository.existsByTherapistAndStartTimeLessThanAndEndTimeGreaterThan(
                therapist, end, start);
    }

}