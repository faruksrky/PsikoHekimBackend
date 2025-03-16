package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.CreateCalendarEventRequest;
import com_psikohekim.psikohekim_appt.dto.request.UpdateCalendarEventRequest;
import com_psikohekim.psikohekim_appt.dto.response.CalendarErrorResponse;
import com_psikohekim.psikohekim_appt.dto.response.CalendarEventResponse;
import com_psikohekim.psikohekim_appt.dto.response.CalendarEventsResponse;
import com_psikohekim.psikohekim_appt.dto.response.CalendarSyncResponse;
import com_psikohekim.psikohekim_appt.exception.CustomExceptionHandler;
import com_psikohekim.psikohekim_appt.exception.ResourceNotFoundException;
import com_psikohekim.psikohekim_appt.model.CustomUserDetails;
import com_psikohekim.psikohekim_appt.service.CalendarService;
import com_psikohekim.psikohekim_appt.service.google.GoogleCalendarIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;
    private final GoogleCalendarIntegrationService googleCalendarService;

    // Takvim etkinliklerini getir
    @GetMapping("/events")
    public ResponseEntity<CalendarEventsResponse> getEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetails) userDetails).getId();
        CalendarEventsResponse events = calendarService.getEvents(userId, start, end);
        return ResponseEntity.ok(events);
    }

    // Yeni etkinlik oluştur
    @PostMapping("/events")
    public ResponseEntity<CalendarEventResponse> createEvent(
            @Valid @RequestBody CreateCalendarEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetails) userDetails).getId();
        CalendarEventResponse event = calendarService.createEvent(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    // Etkinlik güncelle
    @PutMapping("/events/{id}")
    public ResponseEntity<CalendarEventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCalendarEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetails) userDetails).getId();
        CalendarEventResponse event = calendarService.updateEvent(id, request, userId);
        return ResponseEntity.ok(event);
    }

    // Etkinlik sil
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetails) userDetails).getId();
        calendarService.deleteEvent(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Google Calendar entegrasyonu
    @GetMapping("/integrations/google/auth")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl() {
        String authUrl = googleCalendarService.getGoogleOAuthUrl();
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    @GetMapping("/integrations/google/callback")
    public ResponseEntity<CalendarSyncResponse> handleGoogleCallback(
            @RequestParam String code,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetails) userDetails).getId();
        CalendarSyncResponse response = googleCalendarService.handleGoogleCallback(code, userId);
        return ResponseEntity.ok(response);
    }

    // Takvim istatistikleri
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getStatistics(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = ((CustomUserDetails) userDetails).getId();
        Map<String, Long> stats = calendarService.getEventStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    // Hata yakalama
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CalendarErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new CalendarErrorResponse(ex.getMessage(), "NOT_FOUND", LocalDateTime.now()));
    }

    @ExceptionHandler(CustomExceptionHandler.InvalidEventTimeException.class)
    public ResponseEntity<CalendarErrorResponse> handleInvalidTime(CustomExceptionHandler.InvalidEventTimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CalendarErrorResponse(ex.getMessage(), "INVALID_TIME", LocalDateTime.now()));
    }
}
