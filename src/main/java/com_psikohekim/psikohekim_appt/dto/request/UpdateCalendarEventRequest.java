package com_psikohekim.psikohekim_appt.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCalendarEventRequest {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private String location;
    private Boolean isAllDay;
    private String color;
    private Integer reminderMinutes;
    private String recurrenceRule;
    private String status;
}