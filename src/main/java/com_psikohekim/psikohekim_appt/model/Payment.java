package com_psikohekim.psikohekim_appt.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private LocalDateTime paymentDate;
    private BigDecimal amountPatient;
    private BigDecimal amountDoctor;
    private String paymentMethod;
    private String paymentStatus;
    private String description;
    @ManyToOne
    @JoinColumn(name = "patient_Id", nullable = false)
    private Patient patient; // Reference to the patient
}

