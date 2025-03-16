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
import com_psikohekim.psikohekim_appt.repository.CalendarRepository;
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

    public CalendarEventsResponse getEvents(Long userId, LocalDateTime start, LocalDateTime end) {
        List<CalendarEvent> events;

        if (start != null && end != null) {
            events = eventRepository.findByUserIdAndStartTimeBetween(userId, start, end);
        } else {
            events = eventRepository.findByUserId(userId);
        }

        Map<String, Long> stats = getEventStatistics(userId);

        return new CalendarEventsResponse(
                calendarMapper.toResponseList(events),
                events.size(),
                stats
        );
    }

    public CalendarEventResponse createEvent(CreateCalendarEventRequest request, Long userId) {
        validateEventTimes(request.getStartTime(), request.getEndTime());

        if (hasTimeConflict(userId, request.getStartTime(), request.getEndTime())) {
            throw new CustomExceptionHandler.InvalidEventTimeException("Bu zaman aralığında başka bir etkinlik var");
        }

        CalendarEvent event = calendarMapper.toEntity(request);
        event.setUserId(userId);
        event.setSource("MANUAL");
        event.setStatus("CONFIRMED");

        CalendarEvent savedEvent = eventRepository.save(event);
        return CalendarEventResponse.fromEntity(savedEvent);
    }

    public CalendarEventResponse updateEvent(Long eventId, UpdateCalendarEventRequest request, Long userId) {
        CalendarEvent existingEvent = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        if (request.getStartTime() != null && request.getEndTime() != null) {
            validateEventTimes(request.getStartTime(), request.getEndTime());

            // Çakışma kontrolü (kendi etkinliği hariç)
            if (hasTimeConflictExcludingEvent(userId, request.getStartTime(), request.getEndTime(), eventId)) {
                throw new CustomExceptionHandler.InvalidEventTimeException("Bu zaman aralığında başka bir etkinlik var");
            }
        }

        calendarMapper.updateEntityFromRequest(existingEvent, request);
        CalendarEvent updatedEvent = eventRepository.save(existingEvent);
        return CalendarEventResponse.fromEntity(updatedEvent);
    }

    public void deleteEvent(Long eventId, Long userId) {
        CalendarEvent event = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        eventRepository.delete(event);
    }

    public Map<String, Long> getEventStatistics(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        stats.put("total", eventRepository.countByUserId(userId));
        stats.put("upcoming", eventRepository.countByUserIdAndStartTimeAfter(userId, now));
        stats.put("google", eventRepository.countByUserIdAndSource(userId, "GOOGLE"));
        stats.put("outlook", eventRepository.countByUserIdAndSource(userId, "OUTLOOK"));
        stats.put("manual", eventRepository.countByUserIdAndSource(userId, "MANUAL"));

        return stats;
    }

    public CalendarSyncResponse syncEvents(List<CalendarEvent> events, Long userId, String source) {
        try {
            int addedCount = 0;
            int updatedCount = 0;
            List<CalendarEvent> syncedEvents = new ArrayList<>();

            for (CalendarEvent event : events) {
                event.setUserId(userId);
                event.setSource(source);

                // Mevcut etkinliği bul veya yeni oluştur
                CalendarEvent existingEvent = eventRepository
                        .findByExternalIdAndSource(event.getExternalId(), source)
                        .orElse(null);

                if (existingEvent != null) {
                    // Güncelleme
                    existingEvent.setTitle(event.getTitle());
                    existingEvent.setDescription(event.getDescription());
                    existingEvent.setStartTime(event.getStartTime());
                    existingEvent.setEndTime(event.getEndTime());
                    existingEvent.setLocation(event.getLocation());
                    existingEvent.setStatus(event.getStatus());

                    syncedEvents.add(eventRepository.save(existingEvent));
                    updatedCount++;
                } else {
                    // Yeni ekle
                    syncedEvents.add(eventRepository.save(event));
                    addedCount++;
                }
            }

            // İstatistikleri hazırla
            Map<String, Long> statistics = new HashMap<>();
            statistics.put("added", (long) addedCount);
            statistics.put("updated", (long) updatedCount);
            statistics.put("total", (long) events.size());
            statistics.put("lastSyncTime", System.currentTimeMillis());

            return CalendarSyncResponse.builder()
                    .success(true)
                    .events(calendarMapper.toResponseList(syncedEvents))
                    .statistics(statistics)
                    .syncStatus(SyncStatus.COMPLETED)
                    .build();

        } catch (Exception e) {
            return CalendarSyncResponse.builder()
                    .success(false)
                    .error("Takvim senkronizasyonu başarısız: " + e.getMessage())
                    .syncStatus(SyncStatus.FAILED)
                    .build();
        }
    }

    // Senkronizasyon durumunu kontrol et
    public boolean needsSync(Long userId, String source) {
        LocalDateTime lastSyncTime = eventRepository.findLastSyncTimeByUserIdAndSource(userId, source);
        if (lastSyncTime == null) {
            return true;
        }

        // Son 1 saatten eski ise senkronizasyon gerekli
        return lastSyncTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    // Son senkronizasyon zamanını güncelle
    private void updateLastSyncTime(Long userId, String source) {
        eventRepository.updateLastSyncTime(userId, source, LocalDateTime.now());
    }

    private void validateEventTimes(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new CustomExceptionHandler.InvalidEventTimeException("Başlangıç zamanı bitiş zamanından sonra olamaz");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new CustomExceptionHandler.InvalidEventTimeException("Geçmiş bir zamana etkinlik eklenemez");
        }
    }

    private boolean hasTimeConflict(Long userId, LocalDateTime start, LocalDateTime end) {
        return eventRepository.existsByUserIdAndStartTimeLessThanAndEndTimeGreaterThan(
                userId, end, start);
    }

    private boolean hasTimeConflictExcludingEvent(Long userId, LocalDateTime start, LocalDateTime end, Long excludeEventId) {
        return eventRepository.existsByUserIdAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                userId, end, start, excludeEventId);
    }
}