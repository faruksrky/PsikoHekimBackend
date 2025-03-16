package com_psikohekim.psikohekim_appt.mapper;

import com_psikohekim.psikohekim_appt.dto.request.CreateCalendarEventRequest;
import com_psikohekim.psikohekim_appt.dto.request.UpdateCalendarEventRequest;
import com_psikohekim.psikohekim_appt.dto.response.CalendarEventResponse;
import com_psikohekim.psikohekim_appt.model.CalendarEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CalendarMapper {

    public CalendarEvent toEntity(CreateCalendarEventRequest request) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(request.getTitle());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setColor(request.getColor());
        event.setReminderMinutes(request.getReminderMinutes());
        event.setStatus("CONFIRMED");
        event.setSource("MANUAL");
        return event;
    }

    public void updateEntityFromRequest(CalendarEvent event, UpdateCalendarEventRequest request) {
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getColor() != null) event.setColor(request.getColor());
        if (request.getReminderMinutes() != null) event.setReminderMinutes(request.getReminderMinutes());
        if (request.getStatus() != null) event.setStatus(request.getStatus());
    }

    public List<CalendarEventResponse> toResponseList(List<CalendarEvent> events) {
        return events.stream()
                .map(CalendarEventResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
