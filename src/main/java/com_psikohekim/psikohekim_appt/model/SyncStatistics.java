package com_psikohekim.psikohekim_appt.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SyncStatistics {
    private long totalEvents;
    private long addedEvents;
    private long updatedEvents;
    private long deletedEvents;
    private long failedEvents;
    private LocalDateTime lastSyncTime;
}
