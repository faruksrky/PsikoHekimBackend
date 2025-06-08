package com_psikohekim.psikohekim_appt.dto.response;

import com_psikohekim.psikohekim_appt.enums.SyncStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarSyncResponse {
    private boolean success;
    private List<CalendarEventResponse> events;
    private String error;
    private String message;  // Başarı mesajları için
    private Map<String, Long> statistics;
    private SyncStatus syncStatus;

    public CalendarSyncResponse(boolean success, List<CalendarEventResponse> events, String message, Map<String, Long> statistics, SyncStatus syncStatus){
        this.success = success;
        this.events = events;
        this.error = message;
    }

    // Başarılı sync için factory method
    public static CalendarSyncResponse success(List<CalendarEventResponse> events, Map<String, Long> statistics) {
        return CalendarSyncResponse.builder()
                .success(true)
                .events(events)
                .statistics(statistics)
                .syncStatus(SyncStatus.COMPLETED)
                .message("Senkronizasyon başarıyla tamamlandı")
                .build();
    }

    // Hata durumu için factory method
    public static CalendarSyncResponse error(String errorMessage) {
        return CalendarSyncResponse.builder()
                .success(false)
                .error(errorMessage)
                .syncStatus(SyncStatus.FAILED)
                .build();
    }

    // Başarı mesajı için getter (null safety ile)
    public String getSuccessMessage() {
        return success ? message : null;
    }

    // Hata mesajı için getter (null safety ile)
    public String getErrorMessage() {
        return !success ? error : null;
    }
}