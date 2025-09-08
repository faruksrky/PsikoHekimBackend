package com_psikohekim.psikohekim_appt.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Therapist-Patient assignment oluşturma request'i
 * Sadece assignment creation için kullanılır
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAssignmentRequest {

    // ========== CORE ASSIGNMENT ==========
    private Long patientId;
    private Long therapistId;
    private LocalDateTime treatmentStartDate;

    // ========== ASSIGNMENT DETAILS ==========
    private Integer priorityLevel; // 1-5 (1=Düşük, 5=Acil)
    private String primaryDiagnosis;
    private Integer totalSessionsPlanned;
    private String sessionFrequency; // WEEKLY, BIWEEKLY, MONTHLY

    // ========== TREATMENT PLAN ==========
    private String treatmentGoals;
    private String specialRequirements;
    private String therapistNotes;
    private BigDecimal estimatedSessionFee;

    // ========== PREFERENCES ==========
    private String preferredSessionTime; // MORNING, AFTERNOON, EVENING
    private String preferredDays; // MON,WED,FRI
    private String communicationPreference; // EMAIL, SMS, PHONE

    // ========== MEDICAL INFO ==========
    private String referringDoctor;
    private String medicalHistory;
    private String currentMedications;
    private String allergies;

    // ========== ADMINISTRATIVE ==========
    private String assignedBy; // Who is creating this assignment
    private String assignmentReason;
    private String urgencyLevel; // LOW, MEDIUM, HIGH, CRITICAL

    // ========== VALIDATION ==========
    public boolean isValid() {
        return patientId != null &&
                therapistId != null &&
                treatmentStartDate != null &&
                treatmentStartDate.isAfter(LocalDateTime.now());
    }

    public boolean hasValidPriority() {
        return priorityLevel != null &&
                priorityLevel >= 1 &&
                priorityLevel <= 5;
    }

    /**
     * Basit assignment request oluşturucu
     */
    public static CreateAssignmentRequest basic(Long patientId, Long therapistId, LocalDateTime startDate) {
        return CreateAssignmentRequest.builder()
                .patientId(patientId)
                .therapistId(therapistId)
                .treatmentStartDate(startDate)
                .priorityLevel(2) // Normal priority
                .totalSessionsPlanned(10) // Default session count
                .sessionFrequency("WEEKLY")
                .urgencyLevel("MEDIUM")
                .build();
    }

    /**
     * Detaylı assignment request oluşturucu
     */
    public static CreateAssignmentRequest detailed(Long patientId, Long therapistId,
                                                   LocalDateTime startDate, String diagnosis,
                                                   Integer priority, String assignedBy) {
        return CreateAssignmentRequest.builder()
                .patientId(patientId)
                .therapistId(therapistId)
                .treatmentStartDate(startDate)
                .primaryDiagnosis(diagnosis)
                .priorityLevel(priority)
                .assignedBy(assignedBy)
                .totalSessionsPlanned(12)
                .sessionFrequency("WEEKLY")
                .build();
    }
}