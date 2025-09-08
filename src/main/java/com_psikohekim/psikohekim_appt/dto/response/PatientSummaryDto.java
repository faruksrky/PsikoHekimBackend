package com_psikohekim.psikohekim_appt.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientSummaryDto {

    @JsonProperty("assignmentId")
    private Long assignmentId; // TherapistPatient assignment ID
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private String patientPhone;
    private Integer patientAge;
    private String patientGender;

    // İlişki bilgileri
    private LocalDateTime assignedAt;
    private LocalDateTime treatmentStartDate;
    private String assignmentStatus;
    private Integer priorityLevel;

    // Seans bilgileri
    private Integer totalSessionsPlanned;
    private Integer sessionsCompleted;
    private Integer sessionsCancelled;
    private Double completionRate;
    private Double attendanceRate;

    // Son aktivite
    private LocalDateTime lastSessionDate;
    private LocalDateTime nextAppointmentDate;
    private Long daysSinceLastSession;

    // Tedavi bilgileri
    private String primaryDiagnosis;
    private String currentStatus;

    // Finansal bilgiler
    private String outstandingBalance;
    private Boolean hasOutstandingPayments;

    // Helper fields
    private String statusColor; // Frontend için renk kodu
    private String priorityText; // Frontend için öncelik metni
}