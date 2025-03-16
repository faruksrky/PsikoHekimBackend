package com_psikohekim.psikohekim_appt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarErrorResponse {
    private String message;
    private String code;
    private LocalDateTime timestamp = LocalDateTime.now();
}
