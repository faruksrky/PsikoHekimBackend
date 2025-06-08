package com_psikohekim.psikohekim_appt.model;

import com_psikohekim.psikohekim_appt.enums.Experience;
import com_psikohekim.psikohekim_appt.enums.TherapistType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
public class Therapist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long therapistId;
    
    @Column(nullable = false)
    private String therapistFirstName;
    private String therapistLastName;
    private String therapistEmail;
    private String therapistPhoneNumber;
    private String therapistAddress;

    @Enumerated(EnumType.STRING)
    private TherapistType therapistType;

    // Hasta ilişkisi TherapistPatient entity'si üzerinden yönetiliyor
    // @ManyToMany gereksiz - TherapistPatient entity'si kullanılıyor

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<String> therapistSpecializationAreas;

    @Enumerated(EnumType.STRING)
    private Experience therapistYearsOfExperience;

    @OneToMany(mappedBy = "therapist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CalendarEvent> events;

    // Eğitim ve sertifika bilgileri
    private String therapistEducation;
    private String therapistUniversity;
    private String therapistCertifications;
    
    // Finansal bilgiler
    private BigDecimal therapistAppointmentFee;
    
    @Column(name = "therapistRating", nullable = false, columnDefinition = "integer default 30")
    private int therapistRating = 30;
}