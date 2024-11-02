package com_psikohekim.psikohekim_appt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Psychologist {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long psychologistId;
    @Column(nullable = false)
    private String psychologistName;
    private String psychologistSurname;
    private String psychologistEmail;
    private String psychologistPhone;
    private String psychologistAddress;
    private Integer psychologistAge;
    private String psychologistGender;
    private String psychologistBirthday;
    private Integer psychologistExperience;
    @OneToMany(mappedBy = "psychologist")
    private List<Patient> patients; // List of patients
}
