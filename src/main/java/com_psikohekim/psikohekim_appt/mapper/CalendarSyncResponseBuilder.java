package com_psikohekim.psikohekim_appt.mapper;


import com_psikohekim.psikohekim_appt.dto.response.CalendarEventResponse;
import com_psikohekim.psikohekim_appt.dto.response.CalendarSyncResponse;
import com_psikohekim.psikohekim_appt.enums.SyncStatus;

import java.util.List;
import java.util.Map;

// Builder sınıfı
public class CalendarSyncResponseBuilder {
    private boolean success;
    private List<CalendarEventResponse> events;
    private String error;
    private Map<String, Long> statistics;
    private SyncStatus syncStatus;

    public CalendarSyncResponseBuilder success(boolean success) {
        this.success = success;
        return this;
    }

    public CalendarSyncResponseBuilder events(List<CalendarEventResponse> events) {
        this.events = events;
        return this;
    }

    public CalendarSyncResponseBuilder error(String error) {
        this.error = error;
        return this;
    }

    public CalendarSyncResponseBuilder statistics(Map<String, Long> statistics) {
        this.statistics = statistics;
        return this;
    }

    public CalendarSyncResponseBuilder syncStatus(SyncStatus syncStatus) {
        this.syncStatus = syncStatus;
        return this;
    }

    public CalendarSyncResponse build() {
        return new CalendarSyncResponse(success, events, error, statistics, syncStatus);
    }
}
