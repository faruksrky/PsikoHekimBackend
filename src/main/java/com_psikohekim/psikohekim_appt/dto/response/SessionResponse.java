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

    private Long id;
    private Long assignmentId;
    private Long therapistId;
    private Long patientId;

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
    private String paymentStatus;
    private String paymentMethod;

    // Session özellikleri
    private String sessionType;
    private String sessionFormat;
    private Integer sessionRating;
    private String homeworkAssigned;
    private String nextSessionGoals;

    // Helper flags
    private boolean isCompleted;
    private boolean isPaid;
    private boolean isToday;
    private boolean isOverdue;
    private boolean isLateStart;

    // System
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}