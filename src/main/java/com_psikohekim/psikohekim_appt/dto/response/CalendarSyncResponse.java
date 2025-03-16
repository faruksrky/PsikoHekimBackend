package com_psikohekim.psikohekim_appt.dto.response;

import com_psikohekim.psikohekim_appt.enums.SyncStatus;
import com_psikohekim.psikohekim_appt.mapper.CalendarSyncResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarSyncResponse {
    private boolean success;
    private List<CalendarEventResponse> events;
    private String error;
    private Map<String, Long> statistics;
    private SyncStatus syncStatus;

    public CalendarSyncResponse(boolean success, List<CalendarEventResponse> events, String message){
        this.success = success;
        this.events = events;
        this.error = message;
    }

    // Builder pattern ile kolay oluşturma için
    public static CalendarSyncResponseBuilder builder() {
        return new CalendarSyncResponseBuilder();
    }

    // Başarılı sync için factory method
    public static CalendarSyncResponse success(List<CalendarEventResponse> events, Map<String, Long> statistics) {
        return new CalendarSyncResponse(true, events, null, statistics, SyncStatus.COMPLETED);
    }

    // Hata durumu için factory method
    public static CalendarSyncResponse error(String errorMessage) {
        return new CalendarSyncResponse(false, null, errorMessage, null, SyncStatus.FAILED);
    }
}