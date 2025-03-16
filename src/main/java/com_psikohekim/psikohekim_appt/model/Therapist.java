package com_psikohekim.psikohekim_appt.model;

import com_psikohekim.psikohekim_appt.enums.Experience;
import com_psikohekim.psikohekim_appt.enums.TherapistType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private String therapistSurname;
    private String therapistEmail;
    private String therapistPhoneNumber;
    private String therapistAddress;

    @Enumerated(EnumType.STRING)
    private TherapistType therapistType;

    @ManyToMany
    @JoinTable(
            name = "therapist_patient",
            joinColumns = @JoinColumn(name = "therapist_id"),
            inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    private Set<Patient> patients = new HashSet<>();

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<String> specializationAreas;

    @Enumerated(EnumType.STRING)
    private Experience yearsOfExperience;

    @OneToMany(mappedBy = "therapist")
    private List<CalendarEvent> events;

    private String therapistEducation;
    private String therapistUniversity;
    private String therapistCertifications;
    private BigDecimal appointmentFee;
    @Column(name = "therapistRating", nullable = false, columnDefinition = "integer default 30")
    private int therapistRating = 30;

}