package com_psikohekim.psikohekim_appt.enums;

// Senkronizasyon durumu için enum
public enum SyncStatus {
    PENDING("Bekliyor"),
    IN_PROGRESS("Devam Ediyor"),
    COMPLETED("Tamamlandı"),
    FAILED("Başarısız");

    private final String description;

    SyncStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
