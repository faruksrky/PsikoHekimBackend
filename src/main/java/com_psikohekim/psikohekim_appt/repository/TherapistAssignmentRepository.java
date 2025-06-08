package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.TherapistAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface TherapistAssignmentRepository extends JpaRepository<TherapistAssignment, Long> {
    List<TherapistAssignment> findAllByTherapistId(String therapistId);
    Optional<TherapistAssignment> findByProcessInstanceKey (String processInstanceKey);
    boolean existsByTherapistIdAndStatus(String therapistId, TherapistAssignment.AssignmentStatus status);
}
