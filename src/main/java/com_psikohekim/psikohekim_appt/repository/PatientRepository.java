package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {

}
