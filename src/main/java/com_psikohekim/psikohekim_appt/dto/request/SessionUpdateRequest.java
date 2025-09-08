package com_psikohekim.psikohekim_appt.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Session güncelleme/tamamlama request'i
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionUpdateRequest {

    // Session details
    private BigDecimal sessionFee;
    private String sessionType;
    private String sessionFormat;

    // Completion fields
    private String sessionNotes;
    private String therapistNotes;
    private String patientFeedback;
    private String homeworkAssigned;
    private String nextSessionGoals;
    private Integer sessionRating; // 1-5

    // Timing fields
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    // Payment fields
    private String paymentStatus; // PENDING, PAID, PARTIAL, WAIVED
    private String paymentMethod;

    // Cancellation fields
    private String cancellationReason;
    private String cancelledBy; // THERAPIST, PATIENT, SYSTEM

    // Rescheduling
    private LocalDateTime newScheduledDate;

    // Factory methods
    public static SessionUpdateRequest forCompletion(String sessionNotes, String therapistNotes) {
        return SessionUpdateRequest.builder()
                .sessionNotes(sessionNotes)
                .therapistNotes(therapistNotes)
                .actualEndTime(LocalDateTime.now())
                .build();
    }

    public static SessionUpdateRequest forCancellation(String reason, String cancelledBy) {
        return SessionUpdateRequest.builder()
                .cancellationReason(reason)
                .cancelledBy(cancelledBy)
                .build();
    }

    public static SessionUpdateRequest forReschedule(LocalDateTime newDate) {
        return SessionUpdateRequest.builder()
                .newScheduledDate(newDate)
                .build();
    }
}