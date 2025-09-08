package com_psikohekim.psikohekim_appt.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class TherapistRequest {
    private String therapistFirstName;
    private String therapistLastName;
    private String therapistEmail;
    private String therapistPhoneNumber;
    private String therapistAddress;
    private String therapistType;
    private List<String> therapistSpecializationAreas;
    private String therapistYearsOfExperience;
    private String therapistEducation;
    private String therapistCertifications;
    private BigDecimal therapistAppointmentFee;
    private String therapistUniversity;
    private int therapistRating;
}