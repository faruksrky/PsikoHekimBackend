package com_psikohekim.psikohekim_appt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TherapistAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;
    private String patientId;
    private String therapistId;
    private String processInstanceKey;
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AssignmentStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }
    private String processName;
    private String description;
    private String startedBy;
    private String rejectionReason;
    private LocalDateTime rejectedAt;
}
