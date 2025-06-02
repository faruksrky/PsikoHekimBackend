package com_psikohekim.psikohekim_appt.dto.response;

import com_psikohekim.psikohekim_appt.model.TherapistAssignment;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AssignmentResponse {
    private String processInstanceKey;
    private String patientId;
    private String therapistId;
    private String processName;
    private String description;
    private String startedBy;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PatientResponse patientInfo;

    public static AssignmentResponse from(TherapistAssignment assignment, PatientResponse patient) {
        return AssignmentResponse.builder()
                .processInstanceKey(assignment.getProcessInstanceKey())
                .patientId(assignment.getPatientId())
                .therapistId(assignment.getTherapistId())
                .processName(assignment.getProcessName())
                .description(assignment.getDescription())
                .startedBy(assignment.getStartedBy())
                .status(assignment.getStatus().toString())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .patientInfo(patient)
                .build();
    }
}
