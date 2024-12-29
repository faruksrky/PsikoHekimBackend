package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.Therapist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TherapistRepository extends JpaRepository<Therapist, Long> {

    boolean existsByTherapistEmail(String email);
}
