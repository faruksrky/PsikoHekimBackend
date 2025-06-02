package com_psikohekim.psikohekim_appt.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssignTherapistRequest {
    private Long id;
    private String patientId;
    private String therapistId;
    private String processId;
    private String status; // pending, accepted, rejected
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String processName;
    private String description;
    private String startedBy;

}
