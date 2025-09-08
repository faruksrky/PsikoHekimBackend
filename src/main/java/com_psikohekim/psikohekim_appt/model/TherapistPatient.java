package com_psikohekim.psikohekim_appt.model;

import com_psikohekim.psikohekim_appt.enums.AssignmentStatus;
import com_psikohekim.psikohekim_appt.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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
    private Long therapistPatientId;

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

    // Normalized Session Relationship
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TherapySession> sessions = new ArrayList<>();

    // Session sayıları artık bu field'dan hesaplanacak
    @Column(name = "total_sessions_planned")
    private Integer totalSessionsPlanned = 0;

    // Öncelik ve durum
    @Column(name = "priority_level")
    private Integer priorityLevel = 2; // 1-5 arası (1=Düşük, 5=Acil)

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Son aktivite - artık calculated metodlardan gelecek

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

    // ========== CALCULATED METHODS (Denormalized to Normalized) ==========

    /**
     * Tamamlanan session sayısını hesapla
     */
    public int getSessionsCompleted() {
        if (sessions == null) return 0;
        return (int) sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .count();
    }

    /**
     * İptal edilen session sayısını hesapla
     */
    public int getSessionsCancelled() {
        if (sessions == null) return 0;
        return (int) sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.CANCELLED)
                .count();
    }

    /**
     * No-show session sayısını hesapla
     */
    public int getSessionsNoShow() {
        if (sessions == null) return 0;
        return (int) sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.NO_SHOW)
                .count();
    }

    /**
     * Scheduled session sayısını hesapla
     */
    public int getSessionsScheduled() {
        if (sessions == null) return 0;
        return (int) sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.SCHEDULED)
                .count();
    }

    /**
     * Toplam session sayısını hesapla
     */
    public int getTotalSessionsActual() {
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * Outstanding balance hesapla (ödenmemiş session ücreti)
     */
    public BigDecimal getOutstandingBalance() {
        if (sessions == null) return BigDecimal.ZERO;
        return sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .filter(session -> !"PAID".equals(session.getPaymentStatus()))
                .map(TherapySession::getSessionFee)
                .filter(fee -> fee != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Toplam ödenen tutarı hesapla
     */
    public BigDecimal getTotalAmountPaid() {
        if (sessions == null) return BigDecimal.ZERO;
        return sessions.stream()
                .filter(session -> "PAID".equals(session.getPaymentStatus()))
                .map(TherapySession::getSessionFee)
                .filter(fee -> fee != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Completion rate hesapla (tamamlanan / planlanan)
     */
    public double getCompletionRate() {
        if (totalSessionsPlanned == null || totalSessionsPlanned == 0) {
            return 0.0;
        }
        return (double) getSessionsCompleted() / totalSessionsPlanned * 100;
    }

    /**
     * Attendance rate hesapla (gelen / toplam scheduled)
     */
    public double getAttendanceRate() {
        int totalScheduled = getSessionsCompleted() + getSessionsCancelled() + getSessionsNoShow();
        if (totalScheduled == 0) {
            return 0.0;
        }
        return (double) getSessionsCompleted() / totalScheduled * 100;
    }

    /**
     * Son session tarihini hesapla
     */
    public LocalDateTime getLastSessionDate() {
        if (sessions == null || sessions.isEmpty()) return null;
        return sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .map(TherapySession::getScheduledDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    /**
     * Bir sonraki appointment tarihini hesapla
     */
    public LocalDateTime getNextAppointmentDate() {
        if (sessions == null || sessions.isEmpty()) return null;
        return sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.SCHEDULED)
                .filter(session -> session.getScheduledDate().isAfter(LocalDateTime.now()))
                .map(TherapySession::getScheduledDate)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    /**
     * Aktif mi kontrol et
     */
    public boolean isActiveAssignment() {
        return assignmentStatus == AssignmentStatus.ACTIVE &&
                Boolean.TRUE.equals(isActive);
    }

    /**
     * Tamamlanan session'lar listesi
     */
    public List<TherapySession> getCompletedSessions() {
        if (sessions == null) return new ArrayList<>();
        return sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.COMPLETED)
                .sorted((s1, s2) -> s2.getScheduledDate().compareTo(s1.getScheduledDate()))
                .toList();
    }

    /**
     * Scheduled session'lar listesi
     */
    public List<TherapySession> getScheduledSessions() {
        if (sessions == null) return new ArrayList<>();
        return sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.SCHEDULED)
                .sorted((s1, s2) -> s1.getScheduledDate().compareTo(s2.getScheduledDate()))
                .toList();
    }
}