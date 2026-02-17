package com_psikohekim.psikohekim_appt.service;

import com_psikohekim.psikohekim_appt.dto.response.ClientSessionPriceResponse;
import com_psikohekim.psikohekim_appt.dto.response.ConsultantEarningResponse;
import com_psikohekim.psikohekim_appt.model.TherapySession;

public interface PricingService {
    void ensureSplitPricing(TherapySession session);
    void updateClientPaymentStatus(Long sessionId, String paymentStatus);
    ClientSessionPriceResponse getClientSessionPrice(Long sessionId);
    ConsultantEarningResponse getConsultantEarning(Long sessionId);
}
