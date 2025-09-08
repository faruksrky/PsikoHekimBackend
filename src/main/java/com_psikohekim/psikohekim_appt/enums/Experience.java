package com_psikohekim.psikohekim_appt.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Experience {
    ZERO_TO_ONE("0-1 Y"),
    TWO_TO_FIVE("2-5 Y"),
    SIX_TO_TEN("6-10 Y"),
    ELEVEN_TO_FIFTEEN("11-15 Y"),
    SIXTEEN_TO_TWENTY("16-20 Y"),
    TWENTY_TO_PLUS("20+ Y");

    private final String value;

    Experience(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Experience fromString(String value) {
        for (Experience experience : Experience.values()) {
            if (experience.value.equalsIgnoreCase(value)) {
                return experience;
            }
        }
        throw new IllegalArgumentException("Invalid Experience value: " + value);
    }
}