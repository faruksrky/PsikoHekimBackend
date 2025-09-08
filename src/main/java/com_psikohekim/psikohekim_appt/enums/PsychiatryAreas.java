package com_psikohekim.psikohekim_appt.enums;

public enum PsychiatryAreas {
    COCUK_VE_ERGEN_PSIKIYATRISI(0),
    YETISKIN_PSIKIYATRISI(1),
    DIGER(2);

    private final int priority;

    PsychiatryAreas(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}