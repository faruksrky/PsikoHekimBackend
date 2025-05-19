package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.TherapistAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TherapistAssignmentRepository extends JpaRepository<TherapistAssignment, Long> {
    Optional<TherapistAssignment> findByTherapistIdAndStatus(String therapistId, TherapistAssignment.AssignmentStatus status);
    boolean existsByTherapistIdAndStatus(String therapistId, TherapistAssignment.AssignmentStatus status);
}
