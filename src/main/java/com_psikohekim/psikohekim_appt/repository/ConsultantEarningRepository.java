package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.ConsultantEarning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultantEarningRepository extends JpaRepository<ConsultantEarning, Long> {
    Optional<ConsultantEarning> findBySessionId(Long sessionId);
    List<ConsultantEarning> findByConsultantId(Long consultantId);
    List<ConsultantEarning> findBySessionIdIn(List<Long> sessionIds);
}
