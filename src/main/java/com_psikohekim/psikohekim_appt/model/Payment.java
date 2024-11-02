package com_psikohekim.psikohekim_appt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    private Long paymentId;
    private LocalDateTime paymentDate;
    private BigDecimal amountPatient;
    private BigDecimal amountDoctor;
    private String paymentMethod;
    private String paymentStatus;
    private String description;
    @ManyToOne
    @JoinColumn(name = "patientId", nullable = false)
    private Patient patient; // Reference to the patient
}

