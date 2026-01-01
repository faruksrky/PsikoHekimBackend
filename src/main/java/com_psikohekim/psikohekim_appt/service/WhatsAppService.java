package com_psikohekim.psikohekim_appt.service;


import com_psikohekim.psikohekim_appt.model.Patient;
import com_psikohekim.psikohekim_appt.model.Therapist;

import java.time.LocalDateTime;

public interface WhatsAppService {

    /**
     * Randevu onay mesajı gönder
     * @param patient Hasta bilgileri
     * @param therapist Terapist bilgileri
     * @param scheduledDate Randevu tarihi
     */
    void sendAppointmentConfirmation(Patient patient, Therapist therapist, LocalDateTime scheduledDate);
    
    /**
     * Seans planlama bildirimi gönder (hasta onayı için)
     * @param patient Hasta bilgileri
     * @param therapist Terapist bilgileri
     * @param scheduledDate Seans tarihi
     * @param sessionId Seans ID'si (onay linki için)
     * @param sessionFee Seans ücreti
     */
    void sendSessionPlanningNotification(Patient patient, Therapist therapist, 
                                       LocalDateTime scheduledDate, Long sessionId, 
                                       java.math.BigDecimal sessionFee);
    
    /**
     * Terapist'e seans bildirimi gönder
     * @param therapist Terapist bilgileri
     * @param patient Hasta bilgileri
     * @param scheduledDate Seans tarihi
     * @param sessionId Seans ID'si
     */
    void sendTherapistSessionNotification(Therapist therapist, Patient patient, 
                                        LocalDateTime scheduledDate, Long sessionId);
    
    /**
     * Hasta onayı aldıktan sonra teyit mesajı gönder
     * @param patient Hasta bilgileri
     * @param therapist Terapist bilgileri
     * @param scheduledDate Seans tarihi
     * @param sessionId Seans ID'si
     */
    void sendSessionConfirmedNotification(Patient patient, Therapist therapist, 
                                        LocalDateTime scheduledDate, Long sessionId);
}