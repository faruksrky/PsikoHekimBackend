package com_psikohekim.psikohekim_appt.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
public class Patient {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long patientId;

    @Column (nullable = false)
    private String patientFirstName;

    @Column (nullable = false)
    private String patientLastName;

    private String patientGender;
    private Integer patientAge;
    private String patientEmail;

    @Column (nullable = false)
    private String patientPhoneNumber;

    // Adres bilgileri
    private String patientCountry;
    private String patientCity;
    private String patientAddress;

    // Ödeme ilişkisi
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    // Referans bilgisi
    private String patientReference;
}
