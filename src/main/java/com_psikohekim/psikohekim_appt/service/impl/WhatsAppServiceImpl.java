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

import java.math.BigDecimal;
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

    @Override
    public void sendSessionPlanningNotification(Patient patient, Therapist therapist,
                                                LocalDateTime scheduledDate, Long sessionId,
                                                BigDecimal sessionFee) {
        try {
            String message = createSessionPlanningMessage(patient, therapist, scheduledDate, sessionId, sessionFee);
            sendMessage(patient.getPatientPhoneNumber(), message);
            log.info("Session planning notification sent to patient: {} for session: {}",
                    patient.getPatientPhoneNumber(), sessionId);
        } catch (Exception e) {
            log.error("Session planning notification gönderilemedi: {}", e.getMessage());
        }
    }

    @Override
    public void sendTherapistSessionNotification(Therapist therapist, Patient patient,
                                                 LocalDateTime scheduledDate, Long sessionId) {
        try {
            String message = createTherapistNotificationMessage(therapist, patient, scheduledDate, sessionId);
            sendMessage(therapist.getTherapistPhoneNumber(), message);
            log.info("Session notification sent to therapist: {} for session: {}",
                    therapist.getTherapistPhoneNumber(), sessionId);
        } catch (Exception e) {
            log.error("Therapist notification gönderilemedi: {}", e.getMessage());
        }
    }

    @Override
    public void sendSessionConfirmedNotification(Patient patient, Therapist therapist,
                                                 LocalDateTime scheduledDate, Long sessionId) {
        try {
            String message = createSessionConfirmedMessage(patient, therapist, scheduledDate, sessionId);
            sendMessage(patient.getPatientPhoneNumber(), message);
            log.info("Session confirmed notification sent to patient: {} for session: {}",
                    patient.getPatientPhoneNumber(), sessionId);
        } catch (Exception e) {
            log.error("Session confirmed notification gönderilemedi: {}", e.getMessage());
        }
    }

    private String createSessionPlanningMessage(Patient patient, Therapist therapist,
                                                LocalDateTime scheduledDate, Long sessionId,
                                                BigDecimal sessionFee) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = scheduledDate.format(formatter);

        // Onay linki oluştur
        String approvalLink = String.format("https://psikohekim.com/session/approve/%d", sessionId);
        String rejectionLink = String.format("https://psikohekim.com/session/reject/%d", sessionId);

        return String.format(
                "Merhaba %s %s,\n\n" +
                        "🎯 Yeni seans planlandı!\n\n" +
                        "📅 Tarih: %s\n" +
                        "👨‍⚕️ Terapist: %s %s\n" +
                        "💰 Ücret: %s TL\n\n" +
                        "Bu seansı onaylamak için:\n" +
                        "✅ Onayla: %s\n" +
                        "❌ Reddet: %s\n\n" +
                        "Onayınızı bekliyoruz!",
                patient.getPatientFirstName(),
                patient.getPatientLastName(),
                formattedDate,
                therapist.getTherapistFirstName(),
                therapist.getTherapistLastName(),
                sessionFee.toString(),
                approvalLink,
                rejectionLink
        );
    }

    private String createTherapistNotificationMessage(Therapist therapist, Patient patient,
                                                      LocalDateTime scheduledDate, Long sessionId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = scheduledDate.format(formatter);

        return String.format(
                "Merhaba %s %s,\n\n" +
                        "📋 Yeni seans planlandı!\n\n" +
                        "👤 Hasta: %s %s\n" +
                        "📅 Tarih: %s\n" +
                        "🆔 Seans ID: %d\n\n" +
                        "Hasta onayını bekliyoruz. Onay aldıktan sonra seans kesinleşecek.",
                therapist.getTherapistFirstName(),
                therapist.getTherapistLastName(),
                patient.getPatientFirstName(),
                patient.getPatientLastName(),
                formattedDate,
                sessionId
        );
    }

    private String createSessionConfirmedMessage(Patient patient, Therapist therapist,
                                                 LocalDateTime scheduledDate, Long sessionId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = scheduledDate.format(formatter);

        return String.format(
                "Merhaba %s %s,\n\n" +
                        "✅ Seansınız onaylandı!\n\n" +
                        "📅 Tarih: %s\n" +
                        "👨‍⚕️ Terapist: %s %s\n" +
                        "🆔 Seans ID: %d\n\n" +
                        "Randevu saatinde buluşmak üzere!",
                patient.getPatientFirstName(),
                patient.getPatientLastName(),
                formattedDate,
                therapist.getTherapistFirstName(),
                therapist.getTherapistLastName(),
                sessionId
        );
    }

    private void sendMessage(String phoneNumber, String message) {
        log.info("🚀 WhatsApp mesajı gönderiliyor...");
        log.info("📱 Telefon: {}", phoneNumber);
        log.info("💬 Mesaj: {}", message);

        // TEST MODE: Gerçek WhatsApp yerine console'a yazdır
        log.info("🧪 TEST MODE: WhatsApp mesajı simüle ediliyor...");
        log.info("📱 Gerçek WhatsApp numarası: {}", phoneNumber);
        log.info("💬 Gerçek WhatsApp mesajı: {}", message);
        log.info("✅ TEST: WhatsApp mesajı başarıyla gönderildi (simüle)");

        // DEBUG: Credentials'ları kontrol et
        log.info("🔍 Twilio Credentials Debug:");
        log.info("Account SID: {}", twilioAccountSid.isEmpty() ? "BOŞ" : twilioAccountSid);
        log.info("Auth Token: {}", twilioAuthToken.isEmpty() ? "BOŞ" : twilioAuthToken.substring(0, 8) + "...");
        log.info("WhatsApp From: {}", twilioWhatsAppFrom.isEmpty() ? "BOŞ" : twilioWhatsAppFrom);

        if (twilioAccountSid.isEmpty() || twilioAuthToken.isEmpty() || twilioWhatsAppFrom.isEmpty()) {
            log.error("❌ Twilio WhatsApp konfigürasyonu eksik!");
            return;
        }

        // Telefon numarasını temizle ve WhatsApp formatına çevir
        // + işaretini koru, sadece diğer karakterleri temizle
        String cleanPhoneNumber = phoneNumber.replaceAll("[^+\\d]", "");
        
        // + işareti yoksa ekle
        if (!cleanPhoneNumber.startsWith("+")) {
            if (!cleanPhoneNumber.startsWith("90")) {
                cleanPhoneNumber = "+90" + cleanPhoneNumber; // Türkiye kodu ekle
            } else {
                cleanPhoneNumber = "+" + cleanPhoneNumber;
            }
        }
        
        String whatsappTo = "whatsapp:" + cleanPhoneNumber;

        // Twilio WhatsApp API'ye gönder
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(twilioAccountSid, twilioAuthToken);

        String whatsappFrom = "whatsapp:" + twilioWhatsAppFrom;

        // Proper URL encoding for all parameters
        String encodedFrom;
        String encodedTo;
        String encodedBody;
        
        try {
            encodedFrom = java.net.URLEncoder.encode(whatsappFrom, "UTF-8");
            encodedTo = java.net.URLEncoder.encode(whatsappTo, "UTF-8");
            encodedBody = java.net.URLEncoder.encode(message, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            log.error("❌ URL encoding hatası: {}", e.getMessage());
            // Fallback: basic encoding
            encodedFrom = whatsappFrom.replace("+", "%2B");
            encodedTo = whatsappTo.replace("+", "%2B");
            encodedBody = message.replace(" ", "%20");
        }

        String requestBody = String.format(
                "From=%s&To=%s&Body=%s",
                encodedFrom,
                encodedTo,
                encodedBody
        );

        log.info("📱 WhatsApp From: {}", whatsappFrom);
        log.info("📱 WhatsApp To: {}", whatsappTo);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            String twilioUrl = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json";
            log.info("🌐 Twilio URL: {}", twilioUrl);

            ResponseEntity<String> response = restTemplate.postForEntity(twilioUrl, request, String.class);

            log.info("📊 Response Status: {}", response.getStatusCode());
            log.info("📄 Response Body: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ WhatsApp mesajı başarıyla gönderildi: {}", whatsappTo);
            } else {
                log.error("❌ WhatsApp mesajı gönderilemedi: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("💥 Twilio WhatsApp API hatası: {}", e.getMessage());
            log.error("💥 Exception details: ", e);
        }
    }
}