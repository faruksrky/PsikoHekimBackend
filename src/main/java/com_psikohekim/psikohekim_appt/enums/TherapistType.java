package com_psikohekim.psikohekim_appt.enums;

public enum TherapistType {
    PSIKOLOG(0),
    PSIKIYATRIST(1);

    private final int priority;
    TherapistType(int priority) {
        this.priority = priority;
    }
    public int getPriority() {
        return priority;}
}