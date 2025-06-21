package com_psikohekim.psikohekim_appt.mapper;

import com_psikohekim.psikohekim_appt.dto.response.SessionResponse;
import com_psikohekim.psikohekim_appt.model.TherapySession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TherapySession Entity ↔ DTO Mapper
 */
@Component
public class TherapySessionMapper {

    /**
     * Entity → Response DTO
     */
    public SessionResponse toResponseDto(TherapySession session) {
        if (session == null) return null;

        return SessionResponse.builder()
                .sessionId(session.getTherapySessionId())
                .assignmentId(session.getAssignment() != null ? session.getAssignment().getTherapistPatientId() : null)
                .therapistId(session.getTherapistId())
                .patientId(session.getPatientId())

                // Zaman bilgileri
                .scheduledDate(session.getScheduledDate())
                .actualStartTime(session.getActualStartTime())
                .actualEndTime(session.getActualEndTime())
                .durationInMinutes(session.getDurationInMinutes())

                // Session details
                .status(session.getStatus())
                .sessionNotes(session.getSessionNotes())
                .therapistNotes(session.getTherapistNotes())
                .patientFeedback(session.getPatientFeedback())

                // İptal bilgileri
                .cancellationReason(session.getCancellationReason())
                .cancelledBy(session.getCancelledBy())
                .cancelledAt(session.getCancelledAt())

                // Finansal
                .sessionFee(session.getSessionFee())
                .paymentStatus(session.getPaymentStatus())
                .paymentMethod(session.getPaymentMethod())

                // Session özellikleri
                .sessionType(session.getSessionType())
                .sessionFormat(session.getSessionFormat())
                .sessionRating(session.getSessionRating())
                .homeworkAssigned(session.getHomeworkAssigned())
                .nextSessionGoals(session.getNextSessionGoals())

                // Helper flags
                .isCompleted(session.isCompleted())
                .isPaid(session.isPaid())
                .isToday(session.isToday())
                .isOverdue(session.isOverdue())
                .isLateStart(session.isLateStart())

                // System
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    /**
     * Entity List → Response DTO List
     */
    public List<SessionResponse> toResponseDtoList(List<TherapySession> sessions) {
        if (sessions == null) return List.of();

        return sessions.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}