package com_psikohekim.psikohekim_appt.enums;

public enum AppointmentStatus {
    PENDING(0, "Beklemede"),
    CONFIRMED(1, "Onaylandı"),
    CANCELLED(2, "İptal"),
    COMPLETED(3, "Tamamlandı"),
    SCHEDULED(4, "Planlandı"),
    NO_SHOW(5, "Gelmedi"),
    RESCHEDULED(6, "Yeniden Planlandı");

    private final int priority;
    private final String displayName;

    AppointmentStatus(int priority, String displayName) {
        this.priority = priority;
        this.displayName = displayName;
    }

    public int getPriority() {
        return priority;
    }

    public String getDisplayName() {
        return displayName;
    }
}