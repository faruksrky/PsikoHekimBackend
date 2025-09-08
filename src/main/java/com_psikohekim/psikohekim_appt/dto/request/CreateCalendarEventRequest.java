package com_psikohekim.psikohekim_appt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCalendarEventRequest {
    @NotBlank(message = "Başlık zorunludur")
    private String title;

    @NotNull(message = "Başlangıç zamanı zorunludur")
    private LocalDateTime startTime;

    @NotNull(message = "Bitiş zamanı zorunludur")
    private LocalDateTime endTime;

    private String description;
    private String location;
    private String color;
    private Integer reminderMinutes;
    private Long therapistId;
}
