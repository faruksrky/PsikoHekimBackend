package com_psikohekim.psikohekim_appt.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
public class Patient {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long patientID;
    @Column (nullable = false)
    private String patientFirstName;
    @Column (nullable = false)
    private String patientLastName;
    private String patientGender;
    private Integer patientAge;
    private String patientEmail;
    @Column (nullable = false)
    private String patientPhoneNumber;

    @ManyToMany(mappedBy = "patients", fetch = FetchType.EAGER)
    private Set<Therapist> therapists = new HashSet<>();

    private String patientCountry;
    private String patientCity;
    private String patientAddress;
    @OneToMany(mappedBy = "patient")
    private List<Payment> payments;
    private String patientReference;




}
