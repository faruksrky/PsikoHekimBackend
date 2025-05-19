package com_psikohekim.psikohekim_appt.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientResponse {
    private Long patientId;
    private String patientFirstName;
    private String patientLastName;
    private String patientEmail;
    private String patientPhoneNumber;
    private String patientAddress;
    private Integer patientAge;
    private String patientCountry;
    private String patientCity;
    private String patientGender;
    private List<TherapistResponse> therapist;
    private String patientReference;

}