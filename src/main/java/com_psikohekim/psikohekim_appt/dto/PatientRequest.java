package com_psikohekim.psikohekim_appt.dto;

import jakarta.persistence.Column;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PatientRequest {
    private String fistName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private LocalDateTime dateOfBirth;
    private String country;
    private String gender;
}
