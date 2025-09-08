package com_psikohekim.psikohekim_appt.service.impl;

import com_psikohekim.psikohekim_appt.model.Patient;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.service.WhatsAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    @Value("${twilio.account.sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token:}")
    private String twilioAuthToken;

    @Value("${twilio.whatsapp.from:}")
    private String twilioWhatsAppFrom;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendAppointmentConfirmation(Patient patient, Therapist therapist, LocalDateTime scheduledDate) {
        try {
            String message = createAppointmentMessage(patient, therapist, scheduledDate);
            sendMessage(patient.getPatientPhoneNumber(), message);
        } catch (Exception e) {
            // Log error but don't fail the appointment creation
            log.error("WhatsApp mesajı gönderilemedi: {}", e.getMessage());
        }
    }

    private String createAppointmentMessage(Patient patient, Therapist therapist, LocalDateTime scheduledDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = scheduledDate.format(formatter);

        return String.format(
                "Merhaba %s %s,\n\n" +
                        "Randevunuz başarıyla oluşturuldu:\n\n" +
                        "📅 Tarih: %s\n" +
                        "👨‍⚕️ Terapist: %s %s\n\n" +
                        "Randevu saatinde buluşmak üzere!",
                patient.getPatientFirstName(),
                patient.getPatientLastName(),
                formattedDate,
                therapist.getTherapistFirstName(),
                therapist.getTherapistLastName()
        );
    }

    private void sendMessage(String phoneNumber, String message) {
        if (twilioAccountSid.isEmpty() || twilioAuthToken.isEmpty() || twilioWhatsAppFrom.isEmpty()) {
            log.info("Twilio WhatsApp konfigürasyonu eksik. Mesaj: {}", message);
            return;
        }

        // Telefon numarasını temizle ve WhatsApp formatına çevir
        String cleanPhoneNumber = phoneNumber.replaceAll("\\D", "");
        if (!cleanPhoneNumber.startsWith("90")) {
            cleanPhoneNumber = "90" + cleanPhoneNumber; // Türkiye kodu ekle
        }
        String whatsappTo = "whatsapp:+90" + cleanPhoneNumber.substring(2);

        // Twilio WhatsApp API'ye gönder
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(twilioAccountSid, twilioAuthToken);

        String requestBody = String.format(
                "From=%s&To=%s&Body=%s",
                twilioWhatsAppFrom,
                whatsappTo,
                message.replace(" ", "%20")
        );

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            String twilioUrl = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json";
            ResponseEntity<String> response = restTemplate.postForEntity(twilioUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("WhatsApp mesajı başarıyla gönderildi: {}", whatsappTo);
            } else {
                log.error("WhatsApp mesajı gönderilemedi: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Twilio WhatsApp API hatası: {}", e.getMessage());
        }
    }
}