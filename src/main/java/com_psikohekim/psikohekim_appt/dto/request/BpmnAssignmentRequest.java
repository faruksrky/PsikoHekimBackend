package com_psikohekim.psikohekim_appt.dto.request;

import lombok.*;

import java.time.LocalDateTime;

/**
 * BPMN sürecinden gelen assignment request
 * Sadece process tracking için kullanılır
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BpmnAssignmentRequest {

    // ========== BPMN PROCESS CORE ==========
    private String processId;
    private String processInstanceId;
    private String taskId;

    // ========== BASIC ASSIGNMENT INFO ==========
    private Long patientId;
    private Long therapistId;

    // ========== PROCESS METADATA ==========
    private String status; // pending, approved, rejected
    private String startedBy; // Who initiated the process
    private String currentStep;
    private LocalDateTime processStartTime;
    private LocalDateTime lastUpdateTime;

    // ========== PROCESS CONTEXT ==========
    private String processName;
    private String description;
    private String priority; // LOW, MEDIUM, HIGH
    private String source; // WEB, API, SYSTEM

    // ========== VALIDATION ==========
    public boolean isValid() {
        return processId != null &&
                patientId != null &&
                therapistId != null;
    }

    /**
     * Basit BPMN request oluşturucu
     */
    public static BpmnAssignmentRequest create(String processId, Long patientId, Long therapistId, String startedBy) {
        return BpmnAssignmentRequest.builder()
                .processId(processId)
                .patientId(patientId)
                .therapistId(therapistId)
                .startedBy(startedBy)
                .status("PENDING")
                .processStartTime(LocalDateTime.now())
                .build();
    }
}