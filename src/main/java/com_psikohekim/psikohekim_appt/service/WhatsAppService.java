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
}