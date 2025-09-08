package com_psikohekim.psikohekim_appt.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessActionRequest {
    private Long processInstanceKey;
    private String action;
}