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
    /** Frontend uyumluluğu için processInstanceKey ile aynı değer */
    private String processId;
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
        String patientName = patient != null
                ? (patient.getPatientFirstName() + " " + patient.getPatientLastName())
                : ("Hasta #" + assignment.getPatientId());
        String pk = assignment.getProcessInstanceKey();
        return PendingRequest.builder()
                .processInstanceKey(pk)
                .processId(pk)
                .processName(assignment.getProcessName())
                .patientName(patientName)
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
