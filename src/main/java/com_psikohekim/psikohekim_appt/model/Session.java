package com_psikohekim.psikohekim_appt.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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