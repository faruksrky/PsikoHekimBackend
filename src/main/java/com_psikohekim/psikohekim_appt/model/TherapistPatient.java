package com_psikohekim.psikohekim_appt.model;

import com_psikohekim.psikohekim_appt.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "therapist_patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TherapistPatient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", nullable = false)
    private Therapist therapist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Atama bilgileri
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status")
    private AssignmentStatus assignmentStatus = AssignmentStatus.ACTIVE;

    // Tedavi bilgileri
    @Column(name = "treatment_start_date")
    private LocalDateTime treatmentStartDate;

    @Column(name = "treatment_end_date")
    private LocalDateTime treatmentEndDate;

    @Column(name = "primary_diagnosis", length = 500)
    private String primaryDiagnosis;

    @Column(name = "treatment_goals", length = 1000)
    private String treatmentGoals;

    @Column(name = "progress_notes", length = 2000)
    private String progressNotes;

    // Seans istatistikleri
    @Column(name = "total_sessions_planned")
    private Integer totalSessionsPlanned = 0;

    @Column(name = "sessions_completed")
    private Integer sessionsCompleted = 0;

    @Column(name = "sessions_cancelled")
    private Integer sessionsCancelled = 0;

    @Column(name = "sessions_no_show")
    private Integer sessionsNoShow = 0;

    // Finansal bilgiler
    @Column(name = "outstanding_balance", precision = 10, scale = 2)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Column(name = "total_amount_paid", precision = 10, scale = 2)
    private BigDecimal totalAmountPaid = BigDecimal.ZERO;

    // Öncelik ve durum
    @Column(name = "priority_level")
    private Integer priorityLevel = 2; // 1-5 arası (1=Düşük, 5=Acil)

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Son aktivite
    @Column(name = "last_session_date")
    private LocalDateTime lastSessionDate;

    @Column(name = "next_appointment_date")
    private LocalDateTime nextAppointmentDate;

    // Meta bilgiler
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes", length = 1000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (treatmentStartDate == null) {
            treatmentStartDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper metodlar
    public double getCompletionRate() {
        if (totalSessionsPlanned == null || totalSessionsPlanned == 0) {
            return 0.0;
        }
        return (double) (sessionsCompleted != null ? sessionsCompleted : 0) / totalSessionsPlanned * 100;
    }

    public double getAttendanceRate() {
        int totalScheduled = (sessionsCompleted != null ? sessionsCompleted : 0) + 
                           (sessionsCancelled != null ? sessionsCancelled : 0) + 
                           (sessionsNoShow != null ? sessionsNoShow : 0);
        if (totalScheduled == 0) {
            return 0.0;
        }
        return (double) (sessionsCompleted != null ? sessionsCompleted : 0) / totalScheduled * 100;
    }
}