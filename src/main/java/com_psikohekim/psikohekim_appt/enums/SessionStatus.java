package com_psikohekim.psikohekim_appt.enums;

/**
 * Therapy session durumları
 */
public enum SessionStatus {

    SCHEDULED("Planlandı"),
    COMPLETED("Tamamlandı"),
    CANCELLED("İptal Edildi"),
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
        return this == SCHEDULED || this == IN_PROGRESS;
    }

    /**
     * Cancelled status mi kontrol et
     */
    public boolean isCancelled() {
        return this == CANCELLED || this == NO_SHOW;
    }
}