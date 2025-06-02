package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.TherapistPatient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TherapistPatientRepository extends JpaRepository<TherapistPatient, Long> {
}

