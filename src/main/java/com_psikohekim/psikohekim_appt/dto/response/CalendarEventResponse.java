package com_psikohekim.psikohekim_appt.dto.response;

import com_psikohekim.psikohekim_appt.model.CalendarEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventResponse {
    private Long id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private String location;
    private String status;
    private String source;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer reminderMinutes;

    // Entity'den DTO'ya dönüşüm için static factory method
    public static CalendarEventResponse fromEntity(CalendarEvent event) {
        return new CalendarEventResponse(
                event.getId(),
                event.getTitle(),
                event.getStartTime(),
                event.getEndTime(),
                event.getDescription(),
                event.getLocation(),
                event.getStatus(),
                event.getSource(),
                event.getColor(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getReminderMinutes()
        );
    }
}