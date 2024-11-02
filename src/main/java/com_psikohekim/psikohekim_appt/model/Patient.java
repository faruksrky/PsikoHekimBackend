package com_psikohekim.psikohekim_appt.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long patientID; // unique identifier for the patient
    @Column (nullable = false)
    private String firstName; // Patient's first Name
    @Column (nullable = false)
    private String lastName;
    private String gender;// Patient gender
    private LocalDateTime dateOfBirth; // // Patient's date of birth
    private String email; // Patient's email
    @Column (nullable = false)
    private String phoneNumber; // Patient's phone Number
    @ManyToOne
    @JoinColumn(name = "psychologistId", nullable = false)
    private Psychologist psychologist; // Patient's psychologist
    private String country; // Patient's country
    private String address; // Patient's address
    @OneToMany(mappedBy = "patient")
    private List<Payment> payments; // List of payments




}
