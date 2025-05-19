package com_psikohekim.psikohekim_appt.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientRequest {
    private String patientFirstName;
    private String patientLastName;
    private String patientEmail;
    private String patientPhoneNumber;
    private String patientAddress;
    private Integer patientAge;
    private String patientCountry;
    private String patientCity;
    private String patientGender;
    private Long therapistId;
    private String patientReference;

}