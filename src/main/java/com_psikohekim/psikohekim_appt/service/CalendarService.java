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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CalendarService {
    private final CalendarRepository eventRepository;
    private final CalendarMapper calendarMapper;
    private final TherapistRepository therapistRepository;

    public CalendarEventsResponse getEvents(Long therapistId, LocalDateTime start, LocalDateTime end) {
        log.info("Therapist {} için etkinlikler getiriliyor, tarih aralığı: {} - {}", therapistId, start, end);

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Terapist bulunamadı: " + therapistId));

        List<CalendarEvent> events;
        if (start != null && end != null) {
            events = eventRepository.findByTherapistAndStartTimeBetween(therapist, start, end);
        } else {
            events = eventRepository.findByTherapist(therapist);
        }

        Map<String, Long> stats = getEventStatistics(therapist);
        log.info("{} adet etkinlik bulundu", events.size());

        return new CalendarEventsResponse(calendarMapper.toResponseList(events), events.size(), stats);
    }

    public CalendarEventResponse createEvent(CreateCalendarEventRequest request, Long therapistId) {
        log.info("Yeni etkinlik oluşturuluyor: {} - therapistId: {}", request.getTitle(), therapistId);

        validateEventTimes(request.getStartTime(), request.getEndTime());

        if (hasTimeConflict(therapistId, request.getStartTime(), request.getEndTime())) {
            throw new CustomExceptionHandler.InvalidEventTimeException("Bu zaman aralığında başka bir etkinlik var");
        }

        CalendarEvent event = calendarMapper.toEntity(request);
        event.setTherapist(therapistRepository.getReferenceById(therapistId));
        event.setSource("MANUAL");
        event.setStatus("CONFIRMED");

        CalendarEvent savedEvent = eventRepository.save(event);
        log.info("Etkinlik başarıyla oluşturuldu: {}", savedEvent.getId());

        return CalendarEventResponse.fromEntity(savedEvent);
    }

    public CalendarEventResponse updateEvent(Long eventId, UpdateCalendarEventRequest request, Long therapistId) {
        log.info("Etkinlik güncelleniyor: {} - therapistId: {}", eventId, therapistId);

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Terapist bulunamadı: " + therapistId));

        CalendarEvent existingEvent = eventRepository.findByIdAndTherapist(eventId, therapist)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik bulunamadı: " + eventId));

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
        CalendarEvent updatedEvent = eventRepository.save(existingEvent);
        log.info("Etkinlik başarıyla güncellendi: {}", eventId);

        return CalendarEventResponse.fromEntity(updatedEvent);
    }

    private boolean existsByTherapistAndStartTimeLessThanAndEndTimeGreaterThan(
            Therapist therapist,
            LocalDateTime start,
            LocalDateTime end) {
        return eventRepository.existsByTherapistAndStartTimeLessThanAndEndTimeGreaterThan(
                therapist, end, start);
    }

    public void deleteEvent(Long eventId, Long therapistId) {
        log.info("Etkinlik siliniyor: {} - therapistId: {}", eventId, therapistId);

        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Terapist bulunamadı: " + therapistId));

        CalendarEvent event = eventRepository.findByIdAndTherapist(eventId, therapist)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik bulunamadı: " + eventId));

        eventRepository.delete(event);
        log.info("Etkinlik başarıyla silindi: {}", eventId);
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
        log.info("Takvim senkronizasyonu başlıyor: {} adet etkinlik", events.size());

        try {
            int addedCount = 0;
            int updatedCount = 0;
            int skipCount = 0;
            List<CalendarEvent> syncedEvents = new ArrayList<>();

            for (CalendarEvent event : events) {
                try {
                    // Mevcut etkinliği bul veya yeni oluştur
                    CalendarEvent existingEvent = eventRepository
                            .findByExternalIdAndSource(event.getExternalId(), event.getSource())
                            .orElse(null);

                    if (existingEvent != null) {
                        // Güncelleme
                        updateExistingEvent(existingEvent, event);
                        syncedEvents.add(eventRepository.save(existingEvent));
                        updatedCount++;
                        log.debug("Etkinlik güncellendi: {}", event.getExternalId());
                    } else {
                        // Yeni ekle
                        CalendarEvent savedEvent = eventRepository.save(event);
                        syncedEvents.add(savedEvent);
                        addedCount++;
                        log.debug("Yeni etkinlik eklendi: {}", event.getExternalId());
                    }
                } catch (Exception e) {
                    log.warn("Etkinlik senkronize edilemedi: {} - Hata: {}",
                            event.getExternalId(), e.getMessage());
                    skipCount++;
                }
            }

            log.info("Senkronizasyon tamamlandı: {} eklendi, {} güncellendi, {} atlandı",
                    addedCount, updatedCount, skipCount);

            return buildSuccessResponse(syncedEvents, addedCount, updatedCount);

        } catch (Exception e) {
            log.error("Takvim senkronizasyonu başarısız: ", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    private void updateExistingEvent(CalendarEvent existingEvent, CalendarEvent newEvent) {
        existingEvent.setTitle(newEvent.getTitle());
        existingEvent.setDescription(newEvent.getDescription());
        existingEvent.setStartTime(newEvent.getStartTime());
        existingEvent.setEndTime(newEvent.getEndTime());
        existingEvent.setLocation(newEvent.getLocation());
        existingEvent.setStatus(newEvent.getStatus());
        existingEvent.setUpdatedAt(LocalDateTime.now());
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
                .message("Takvim senkronizasyonu başarıyla tamamlandı")
                .events(calendarMapper.toResponseList(syncedEvents))
                .statistics(statistics)
                .syncStatus(SyncStatus.COMPLETED)
                .build();
    }

    public CalendarSyncResponse buildErrorResponse(String errorMessage) {
        log.error("Senkronizasyon hatası: {}", errorMessage);

        return CalendarSyncResponse.builder()
                .success(false)
                .error("Takvim senkronizasyonu başarısız: " + errorMessage)
                .message("Senkronizasyon sırasında bir hata oluştu")
                .syncStatus(SyncStatus.FAILED)
                .build();
    }

    private void validateEventTimes(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        if (start.isAfter(end)) {
            throw new CustomExceptionHandler.InvalidEventTimeException(
                    "Başlangıç zamanı bitiş zamanından sonra olamaz"
            );
        }

        if (start.isBefore(now.minusMinutes(5))) { // 5 dakika tolerans
            throw new CustomExceptionHandler.InvalidEventTimeException(
                    "Geçmiş bir zamana etkinlik eklenemez"
            );
        }
    }

    private boolean hasTimeConflict(Long therapistId, LocalDateTime start, LocalDateTime end) {
        Therapist therapist = therapistRepository.findById(therapistId)
                .orElseThrow(() -> new ResourceNotFoundException("Terapist bulunamadı: " + therapistId));

        return eventRepository.existsByTherapistAndStartTimeLessThanAndEndTimeGreaterThan(
                therapist, end, start);
    }
}