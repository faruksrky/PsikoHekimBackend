package com_psikohekim.psikohekim_appt.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class CustomErrorResponse {
    private String message;
    private int status;

    public CustomErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
