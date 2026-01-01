package com_psikohekim.psikohekim_appt.enums;

/**
 * Therapy session durumları
 */
public enum SessionStatus {

    SCHEDULED("Planlandı"),
    PENDING_APPROVAL("Hasta Onayı Bekliyor"),
    COMPLETED("Tamamlandı"),
    CANCELLED("İptal Edildi"),
    REJECTED("Hasta Tarafından Reddedildi"),
    NO_SHOW("Hasta Gelmedi"),
    IN_PROGRESS("Devam Ediyor"),
    RESCHEDULED("Ertelendi");

    private final String description;

    SessionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Completed session mi kontrol et
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * Active session mi kontrol et (tamamlanmamış)
     */
    public boolean isActive() {
        return this == SCHEDULED || this == IN_PROGRESS || this == PENDING_APPROVAL;
    }

    /**
     * Cancelled status mi kontrol et
     */
    public boolean isCancelled() {
        return this == CANCELLED || this == NO_SHOW || this == REJECTED;
    }
}