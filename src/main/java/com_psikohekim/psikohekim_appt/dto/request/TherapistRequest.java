package com_psikohekim.psikohekim_appt.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class TherapistRequest {

    private String therapistFirstName;
    private String therapistSurname;
    private String therapistEmail;
    private String therapistPhoneNumber;
    private String therapistAddress;
    private String therapistType;
    private List<String> specializationAreas;
    private String yearsOfExperience;
    private String therapistEducation;
    private String therapistCertifications;
    private BigDecimal appointmentFee;
    private String therapistUniversity;
    private int therapistRating;
}