package com_psikohekim.psikohekim_appt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PsychologistRequest {

    private String name;
    private String surname;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
}
