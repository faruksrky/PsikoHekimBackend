package com_psikohekim.psikohekim_appt.model;

import com_psikohekim.psikohekim_appt.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Bireysel therapy session entity
 * Normalized session tracking için
 */
@Entity
@Table(name = "therapy_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TherapySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long therapySessionId;

    // ========== İLİŞKİLER ==========
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private TherapistPatient assignment;

    // Direkt ilişkiler (performans için)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id", insertable = false, updatable = false)
    private Therapist therapist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;

    // Kolay erişim için denormalized (optional optimization)
    @Column(name = "therapist_id")
    private Long therapistId;

    @Column(name = "patient_id")
    private Long patientId;

    // ========== ZAMAN BİLGİLERİ ==========
    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    // ========== SESSION DETAYLARI ==========
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Column(name = "session_notes", length = 2000)
    private String sessionNotes;

    @Column(name = "therapist_notes", length = 1000)
    private String therapistNotes;

    @Column(name = "patient_feedback", length = 1000)
    private String patientFeedback;

    // ========== İPTAL BİLGİLERİ ==========
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_by") // THERAPIST, PATIENT, SYSTEM
    private String cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ========== FİNANSAL BİLGİLER ==========
    @Column(name = "session_fee", precision = 10, scale = 2)
    private BigDecimal sessionFee;

    @Column(name = "payment_status") // PENDING, PAID, PARTIAL, WAIVED
    private String paymentStatus = "PENDING";

    @Column(name = "payment_method")
    private String paymentMethod;

    // ========== SEANS TÜRÜ ==========
    @Column(name = "session_type") // INITIAL, REGULAR, FOLLOWUP, FINAL
    private String sessionType = "REGULAR";

    @Column(name = "session_format") // IN_PERSON, ONLINE, PHONE
    private String sessionFormat = "IN_PERSON";

    // ========== KALİTE KONTROL ==========
    @Column(name = "session_rating") // 1-5 hasta değerlendirmesi
    private Integer sessionRating;

    @Column(name = "homework_assigned")
    private String homeworkAssigned;

    @Column(name = "next_session_goals", length = 500)
    private String nextSessionGoals;

    // ========== SYSTEM FIELDS ==========
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // ========== LIFECYCLE HOOKS ==========
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Assignment'tan bilgileri kopyala
        if (assignment != null) {
            therapistId = assignment.getTherapist().getTherapistId();
            patientId = assignment.getPatient().getPatientId();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== HELPER METHODS ==========

    /**
     * Session süresi hesapla (dakika)
     */
    public Long getDurationInMinutes() {
        if (actualStartTime != null && actualEndTime != null) {
            return Duration.between(actualStartTime, actualEndTime).toMinutes();
        }
        return null;
    }

    /**
     * Session geç mi başladı?
     */
    public boolean isLateStart() {
        if (actualStartTime != null && scheduledDate != null) {
            return actualStartTime.isAfter(scheduledDate.plusMinutes(15)); // 15 dk tolerans
        }
        return false;
    }

    /**
     * Session bugün mü?
     */
    public boolean isToday() {
        return scheduledDate != null &&
                scheduledDate.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    /**
     * Session geçmişte kaldı mı?
     */
    public boolean isOverdue() {
        return scheduledDate != null &&
                scheduledDate.isBefore(LocalDateTime.now()) &&
                status == SessionStatus.SCHEDULED;
    }

    /**
     * Session tamamlanmış mı?
     */
    public boolean isCompleted() {
        return status == SessionStatus.COMPLETED;
    }

    /**
     * Ödeme yapılmış mı?
     */
    public boolean isPaid() {
        return "PAID".equals(paymentStatus);
    }

    /**
     * Session completion için helper
     */
    public void markAsCompleted(String notes, String therapistNotes) {
        this.status = SessionStatus.COMPLETED;
        this.sessionNotes = notes;
        this.therapistNotes = therapistNotes;
        if (this.actualStartTime == null) {
            this.actualStartTime = this.scheduledDate;
        }
        if (this.actualEndTime == null) {
            this.actualEndTime = LocalDateTime.now();
        }
    }

    /**
     * Session cancellation için helper
     */
    public void markAsCancelled(String reason, String cancelledBy) {
        this.status = SessionStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledBy = cancelledBy;
        this.cancelledAt = LocalDateTime.now();
    }
}