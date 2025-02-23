package com_psikohekim.psikohekim_appt.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class TherapistResponse {
    private Long therapistId;
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
