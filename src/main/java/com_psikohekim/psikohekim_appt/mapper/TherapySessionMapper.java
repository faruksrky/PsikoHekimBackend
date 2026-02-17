package com_psikohekim.psikohekim_appt.mapper;

import com_psikohekim.psikohekim_appt.dto.response.SessionResponse;
import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.dto.response.TherapistResponse;
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

        // Patient bilgilerini hazırla (direkt ilişkiden)
        PatientResponse patient = null;
        if (session.getPatient() != null) {
            var patientEntity = session.getPatient();
            patient = PatientResponse.builder()
                    .patientId(patientEntity.getPatientId())
                    .patientFirstName(patientEntity.getPatientFirstName())
                    .patientLastName(patientEntity.getPatientLastName())
                    .patientEmail(patientEntity.getPatientEmail())
                    .patientPhoneNumber(patientEntity.getPatientPhoneNumber())
                    .patientAddress(patientEntity.getPatientAddress())
                    .patientAge(patientEntity.getPatientAge())
                    .patientCountry(patientEntity.getPatientCountry())
                    .patientCity(patientEntity.getPatientCity())
                    .patientGender(patientEntity.getPatientGender())
                    .patientReference(patientEntity.getPatientReference())
                    .build();
        }

        // Therapist bilgilerini hazırla (direkt ilişkiden)
        TherapistResponse therapist = null;
        if (session.getTherapist() != null) {
            var therapistEntity = session.getTherapist();
            therapist = TherapistResponse.builder()
                    .therapistId(therapistEntity.getTherapistId())
                    .therapistFirstName(therapistEntity.getTherapistFirstName())
                    .therapistLastName(therapistEntity.getTherapistLastName())
                    .therapistEmail(therapistEntity.getTherapistEmail())
                    .therapistPhoneNumber(therapistEntity.getTherapistPhoneNumber())
                    .therapistAddress(therapistEntity.getTherapistAddress())
                    .therapistType(therapistEntity.getTherapistType() != null ? therapistEntity.getTherapistType().toString() : null)
                    .therapistSpecializationAreas(therapistEntity.getTherapistSpecializationAreas())
                    .therapistYearsOfExperience(therapistEntity.getTherapistYearsOfExperience() != null ? therapistEntity.getTherapistYearsOfExperience().getValue() : null)
                    .therapistEducation(therapistEntity.getTherapistEducation())
                    .therapistCertifications(therapistEntity.getTherapistCertifications())
                    .therapistAppointmentFee(therapistEntity.getTherapistAppointmentFee())
                    .therapistAppointmentFeeCurrency(therapistEntity.getTherapistAppointmentFeeCurrency())
                    .therapistConsultantFee(therapistEntity.getTherapistConsultantFee())
                    .therapistConsultantFeeCurrency(therapistEntity.getTherapistConsultantFeeCurrency())
                    .therapistUniversity(therapistEntity.getTherapistUniversity())
                    .therapistRating(therapistEntity.getTherapistRating())
                    .build();
        }

        return SessionResponse.builder()
                .sessionId(session.getTherapySessionId())
                .assignmentId(session.getAssignment() != null ? session.getAssignment().getTherapistPatientId() : null)
                .therapistId(session.getTherapistId())
                .patientId(session.getPatientId())
                .patient(patient)
                .therapist(therapist)

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

                // Cancellation info
                .cancellationReason(session.getCancellationReason())
                .cancelledBy(session.getCancelledBy())
                .cancelledAt(session.getCancelledAt())

                // Financial info
                .sessionFee(session.getSessionFee())
                .sessionFeeCurrency(session.getSessionFeeCurrency())
                .paymentStatus(session.getPaymentStatus())
                .paymentMethod(session.getPaymentMethod())

                // Session type info
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

                // System fields
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