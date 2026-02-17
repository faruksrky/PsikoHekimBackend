package com_psikohekim.psikohekim_appt.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TherapistResponse {
    private Long therapistId;
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
    private String therapistAppointmentFeeCurrency;
    private BigDecimal therapistConsultantFee;
    private String therapistConsultantFeeCurrency;
    private String therapistUniversity;
    private int therapistRating;
}
