package com_psikohekim.psikohekim_appt.dto.request;

import com_psikohekim.psikohekim_appt.dto.response.PatientResponse;
import com_psikohekim.psikohekim_appt.model.TherapistAssignment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PendingRequest {
    private String processInstanceKey;
    private String processName;
    private String patientName;
    private String therapistId;
    private TherapistAssignment.AssignmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledDate;
    private String sessionType;
    private String description;
    private String startedBy;

    public static PendingRequest from(TherapistAssignment assignment, PatientResponse patient) {
        return PendingRequest.builder()
                .processInstanceKey(assignment.getProcessInstanceKey())
                .processName(assignment.getProcessName())
                .patientName(patient.getPatientFirstName() + " " + patient.getPatientLastName())
                .therapistId(assignment.getTherapistId())
                .status(assignment.getStatus())
                .createdAt(assignment.getCreatedAt())
                .scheduledDate(assignment.getScheduledDate())
                .sessionType(assignment.getSessionType())
                .description(assignment.getDescription())
                .startedBy(assignment.getStartedBy())
                .build();
    }
}
