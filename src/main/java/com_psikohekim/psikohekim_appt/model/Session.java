package com_psikohekim.psikohekim_appt.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;
    private Long patientId;
    private Long psychologistId;
    private LocalDateTime sessionDate;
    private LocalDateTime sessionTime;
    private Integer duration; // Dakikalar cinsinden
    private String sessionType;
    private String notes;
    private String status;
    private Long paymentID;
    private BigDecimal fee;
}
