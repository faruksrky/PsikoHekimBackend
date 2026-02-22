package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.dto.response.ClientSessionPriceResponse;
import com_psikohekim.psikohekim_appt.dto.response.ConsultantEarningResponse;
import com_psikohekim.psikohekim_appt.model.ClientSessionPrice;
import com_psikohekim.psikohekim_appt.model.ConsultantEarning;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.model.TherapySession;
import com_psikohekim.psikohekim_appt.repository.ClientSessionPriceRepository;
import com_psikohekim.psikohekim_appt.repository.ConsultantEarningRepository;
import com_psikohekim.psikohekim_appt.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingServiceImpl implements PricingService {

    private final ClientSessionPriceRepository clientSessionPriceRepository;
    private final ConsultantEarningRepository consultantEarningRepository;

    @Override
    public void ensureSplitPricing(TherapySession session) {
        if (session == null || session.getTherapySessionId() == null) {
            return;
        }

        Long sessionId = session.getTherapySessionId();
        BigDecimal sessionFee = session.getSessionFee() != null ? session.getSessionFee() : BigDecimal.ZERO;

        Therapist therapist = session.getTherapist();
        BigDecimal clientPrice;
        BigDecimal consultantFee;

        if (therapist != null && therapist.getTherapistAppointmentFee() != null && therapist.getTherapistConsultantFee() != null) {
            // Ciro: Danışan ödemesi (appointmentFee), Hakediş: Danışman payı (consultantFee)
            clientPrice = therapist.getTherapistAppointmentFee();
            consultantFee = therapist.getTherapistConsultantFee();
        } else {
            // Geriye dönük uyumluluk: ikisi de sessionFee
            clientPrice = sessionFee;
            consultantFee = sessionFee;
        }

        clientSessionPriceRepository.findBySessionId(sessionId)
                .map(existing -> {
                    existing.setSessionPrice(clientPrice);
                    return clientSessionPriceRepository.save(existing);
                })
                .orElseGet(() -> clientSessionPriceRepository.save(ClientSessionPrice.builder()
                        .sessionId(sessionId)
                        .clientId(session.getPatientId())
                        .sessionPrice(clientPrice)
                        .currency("TRY")
                        .paymentStatus(session.getPaymentStatus() != null ? session.getPaymentStatus() : "PENDING")
                        .build()));

        consultantEarningRepository.findBySessionId(sessionId)
                .map(existing -> {
                    existing.setConsultantFee(consultantFee);
                    return consultantEarningRepository.save(existing);
                })
                .orElseGet(() -> consultantEarningRepository.save(ConsultantEarning.builder()
                        .sessionId(sessionId)
                        .consultantId(session.getTherapistId())
                        .consultantFee(consultantFee)
                        .payoutStatus("PENDING")
                        .build()));
    }

    @Override
    public void updateClientPaymentStatus(Long sessionId, String paymentStatus) {
        if (sessionId == null || paymentStatus == null) {
            return;
        }

        clientSessionPriceRepository.findBySessionId(sessionId).ifPresent(clientPrice -> {
            clientPrice.setPaymentStatus(paymentStatus);
            clientSessionPriceRepository.save(clientPrice);
        });

        if ("PAID".equalsIgnoreCase(paymentStatus)) {
            consultantEarningRepository.findBySessionId(sessionId).ifPresent(earning -> {
                if ("PENDING".equalsIgnoreCase(earning.getPayoutStatus())) {
                    earning.setPayoutStatus("READY");
                    consultantEarningRepository.save(earning);
                }
            });
        }
    }

    @Override
    public ClientSessionPriceResponse getClientSessionPrice(Long sessionId) {
        return clientSessionPriceRepository.findBySessionId(sessionId)
                .map(price -> ClientSessionPriceResponse.builder()
                        .sessionId(price.getSessionId())
                        .clientId(price.getClientId())
                        .sessionPrice(price.getSessionPrice())
                        .currency(price.getCurrency())
                        .paymentStatus(price.getPaymentStatus())
                        .build())
                .orElse(null);
    }

    @Override
    public ConsultantEarningResponse getConsultantEarning(Long sessionId) {
        return consultantEarningRepository.findBySessionId(sessionId)
                .map(earning -> ConsultantEarningResponse.builder()
                        .sessionId(earning.getSessionId())
                        .consultantId(earning.getConsultantId())
                        .consultantFee(earning.getConsultantFee())
                        .payoutStatus(earning.getPayoutStatus())
                        .build())
                .orElse(null);
    }
}
