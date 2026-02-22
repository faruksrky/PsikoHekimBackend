package com_psikohekim.psikohekim_appt.dto.response;

import com_psikohekim.psikohekim_appt.enums.SessionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Session response DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionResponse {

    private Long sessionId;
    private Long assignmentId;
    private Long therapistId;
    private Long patientId;

    // Patient ve Therapist bilgileri
    private PatientResponse patient;
    private TherapistResponse therapist;

    // Zaman bilgileri
    private LocalDateTime scheduledDate;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Long durationInMinutes;

    // Session details
    private SessionStatus status;
    private String sessionNotes;
    private String therapistNotes;
    private String patientFeedback;

    // Iptal bilgileri
    private String cancellationReason;
    private String cancelledBy;
    private LocalDateTime cancelledAt;

    // Finansal
    private BigDecimal sessionFee;
    private String sessionFeeCurrency;
    /** Ciro: Danışan ödemesi (ClientSessionPrice) - Finans gelir listesinde kullanılır */
    private BigDecimal clientPrice;
    @Builder.Default
    private String paymentStatus = "PENDING";
    private String paymentMethod;

    // Session özellikleri
    @Builder.Default
    private String sessionType = "REGULAR";
    @Builder.Default
    private String sessionFormat = "IN_PERSON";
    private Integer sessionRating;
    private String homeworkAssigned;
    private String nextSessionGoals;

    // Helper flags
    @Builder.Default
    private boolean isCompleted = false;
    @Builder.Default
    private boolean isPaid = false;
    @Builder.Default
    private boolean isToday = false;
    @Builder.Default
    private boolean isOverdue = false;
    @Builder.Default
    private boolean isLateStart = false;

    // System
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}