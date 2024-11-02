package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.Psychologist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PsychologistRepository extends JpaRepository<Psychologist, Long> {
}
