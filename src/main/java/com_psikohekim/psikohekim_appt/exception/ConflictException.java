package com_psikohekim.psikohekim_appt.exception;

public class ConflictException extends Exception {

    private String fieldName;

    public ConflictException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}