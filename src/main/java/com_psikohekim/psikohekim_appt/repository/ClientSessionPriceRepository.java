package com_psikohekim.psikohekim_appt.repository;

import com_psikohekim.psikohekim_appt.model.ClientSessionPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientSessionPriceRepository extends JpaRepository<ClientSessionPrice, Long> {
    Optional<ClientSessionPrice> findBySessionId(Long sessionId);
    List<ClientSessionPrice> findBySessionIdIn(List<Long> sessionIds);
    List<ClientSessionPrice> findByClientId(Long clientId);
}
