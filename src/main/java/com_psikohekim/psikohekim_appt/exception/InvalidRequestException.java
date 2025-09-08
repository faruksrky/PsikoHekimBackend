package com_psikohekim.psikohekim_appt.exception;

public class InvalidRequestException extends Exception {

    private String fieldName;

    public InvalidRequestException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }


}
