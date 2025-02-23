package com_psikohekim.psikohekim_appt.model;

import com.google.api.client.util.DateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class CalendarEvent {

    private String title; // Etkinlik başlığı
    private String startTime; // Başlangıç zamanı
    private String endTime; // Bitiş zamanı
    private String description; // Etkinlik açıklaması
    private String location; // Etkinlik konumu
}
