package com_psikohekim.psikohekim_appt.enums;

public enum PsychologistAreas {
    KLINIK_PSIKOLOJI(0),
    GELISIM_PSIKOLOJISI(1),
    AILE_VE_CIFT_TERAPISI(2),
    EGITIM_PSIKOLOJISI(3),
    DIGER(4);

    private final int priority;

    PsychologistAreas(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;}
}