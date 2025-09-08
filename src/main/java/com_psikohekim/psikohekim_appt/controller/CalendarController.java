package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.dto.request.CreateCalendarEventRequest;
import com_psikohekim.psikohekim_appt.dto.request.UpdateCalendarEventRequest;
import com_psikohekim.psikohekim_appt.dto.response.CalendarEventResponse;
import com_psikohekim.psikohekim_appt.dto.response.CalendarEventsResponse;
import com_psikohekim.psikohekim_appt.service.CalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/events")
    public ResponseEntity<CalendarEventsResponse> getEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam Long therapistId
    ) {
        CalendarEventsResponse events = calendarService.getEvents(therapistId, start, end);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/events")
    public ResponseEntity<CalendarEventResponse> createEvent(
            @Valid @RequestBody CreateCalendarEventRequest request
    ) {
        Long therapistId = request.getTherapistId();
        CalendarEventResponse event = calendarService.createEvent(request, therapistId);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<CalendarEventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateCalendarEventRequest request,
            @RequestParam Long therapistId
    ) {
        CalendarEventResponse event = calendarService.updateEvent(eventId, request, therapistId);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long eventId,
            @RequestParam Long therapistId
    ) {
        calendarService.deleteEvent(eventId, therapistId);
        return ResponseEntity.noContent().build();
    }
}