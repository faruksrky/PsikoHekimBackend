package com_psikohekim.psikohekim_appt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarSyncRequest {
    @NotBlank(message = "Takvim kaynağı zorunludur")
    private String source; // GOOGLE veya OUTLOOK
    private String accessToken;
}