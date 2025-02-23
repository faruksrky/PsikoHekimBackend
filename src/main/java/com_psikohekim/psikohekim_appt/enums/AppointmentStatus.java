package com_psikohekim.psikohekim_appt.enums;

public enum AppointmentStatus {
    BEKLEMEDE(0),
    ONAYLANDI(1),
    IPTAL(2);

    private final int priority;

    AppointmentStatus(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}