package com_psikohekim.psikohekim_appt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AssignResponse {
    private boolean success;
    private String message;
}