package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.service.google.GoogleCalendarIntegrationService;
import com_psikohekim.psikohekim_appt.dto.response.CalendarSyncResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/google-calendar")
@RequiredArgsConstructor
public class GoogleController {

    private final GoogleCalendarIntegrationService googleCalendarService;

    @Value("${app.frontend.url:http://localhost:3031}")
    private String frontendUrl;

    @GetMapping("/redirect-to-google-oauth")
    public ResponseEntity<?> getGoogleAuthUrl(@RequestParam Long therapistId) {
        try {
            log.info("Google OAuth URL isteniyor, therapistId: {}", therapistId);

            String state = "therapistId=" + therapistId;
            String authUrl = googleCalendarService.getGoogleOAuthUrl(state);

            log.info("Google OAuth URL başarıyla oluşturuldu");

            return ResponseEntity.ok(Map.of(
                    "authUrl", authUrl,
                    "message", "Google Calendar'a yönlendiriliyorsunuz..."
            ));

        } catch (Exception e) {
            log.error("Google OAuth URL oluşturulamadı: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "OAuth URL oluşturulamadı: " + e.getMessage(),
                            "success", false
                    ));
        }
    }

    @GetMapping("/callback")
    public void handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            HttpServletResponse response) throws IOException {

        log.info("Google callback alındı - code: {}, state: {}, error: {}",
                code != null ? "var" : "yok", state, error);

        try {
            // OAuth hatası kontrolü
            if (error != null) {
                handleOAuthError(error, response);
                return;
            }

            // Parametrelerin varlığını kontrol et
            if (code == null || state == null) {
                handleParameterError(response);
                return;
            }

            // TherapistId'yi state'den çıkar
            Long therapistId = extractTherapistIdFromState(state);
            log.info("TherapistId çıkarıldı: {}", therapistId);

            // Google Calendar senkronizasyonu
            CalendarSyncResponse syncResponse = googleCalendarService.handleGoogleCallback(code, therapistId);

            // Sonuçlara göre yönlendirme
            if (syncResponse.isSuccess()) {
                redirectToSuccess(syncResponse, response);
            } else {
                redirectToError(syncResponse.getError(), response);
            }

        } catch (Exception e) {
            log.error("Google callback işlenirken hata: ", e);
            redirectToError("Google Calendar senkronizasyonu sırasında beklenmeyen bir hata oluştu", response);
        }
    }

    private void handleOAuthError(String error, HttpServletResponse response) throws IOException {
        log.warn("Google OAuth hatası: {}", error);

        String errorMessage = switch (error) {
            case "access_denied" -> "Google Calendar erişimi reddedildi. Lütfen izinleri onaylayın.";
            case "invalid_request" -> "Geçersiz istek. Lütfen tekrar deneyin.";
            case "unauthorized_client" -> "Yetkisiz istemci. Lütfen sistem yöneticisine başvurun.";
            case "unsupported_response_type" -> "Desteklenmeyen yanıt türü.";
            case "invalid_scope" -> "Geçersiz yetki kapsamı.";
            case "server_error" -> "Google sunucu hatası. Lütfen daha sonra tekrar deneyin.";
            case "temporarily_unavailable" -> "Google servisi geçici olarak kullanılamıyor.";
            default -> "Google OAuth hatası: " + error;
        };

        redirectToError(errorMessage, response);
    }

    private void handleParameterError(HttpServletResponse response) throws IOException {
        log.error("Gerekli parametreler eksik");
        redirectToError("Gerekli parametreler eksik. Lütfen tekrar deneyin.", response);
    }

    private void redirectToSuccess(CalendarSyncResponse syncResponse, HttpServletResponse response) throws IOException {
        try {
            StringBuilder url = new StringBuilder(frontendUrl + "/dashboard/calendar?sync=success");

            // Başarı mesajı
            String message = "Google Calendar senkronizasyonu başarıyla tamamlandı!";
            url.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));

            // İstatistikler
            if (syncResponse.getStatistics() != null) {
                Long added = syncResponse.getStatistics().get("added");
                Long updated = syncResponse.getStatistics().get("updated");
                Long total = syncResponse.getStatistics().get("total");

                if (added != null) url.append("&added=").append(added);
                if (updated != null) url.append("&updated=").append(updated);
                if (total != null) url.append("&total=").append(total);

                // Detaylı mesaj
                String detailMessage = String.format("%d etkinlik eklendi, %d etkinlik güncellendi",
                        added != null ? added : 0,
                        updated != null ? updated : 0);
                url.append("&details=").append(URLEncoder.encode(detailMessage, StandardCharsets.UTF_8));
            }

            log.info("Başarı sayfasına yönlendiriliyor: {}", url.toString());
            response.sendRedirect(url.toString());

        } catch (Exception e) {
            log.error("Başarı redirect'i oluşturulamadı: ", e);
            redirectToError("Yönlendirme hatası oluştu", response);
        }
    }

    private void redirectToError(String errorMessage, HttpServletResponse response) throws IOException {
        try {
            String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
            String redirectUrl = frontendUrl + "/dashboard/calendar?sync=error&message=" + encodedMessage;

            log.info("Hata sayfasına yönlendiriliyor: {}", errorMessage);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Error redirect'i oluşturulamadı: ", e);
            // Son çare olarak basit hata sayfası
            response.sendRedirect(frontendUrl + "/dashboard/calendar?sync=error&message=Bilinmeyen+hata");
        }
    }

    private Long extractTherapistIdFromState(String state) {
        try {
            if (state == null || state.trim().isEmpty()) {
                throw new IllegalArgumentException("State parametresi boş");
            }

            String decoded = URLDecoder.decode(state, StandardCharsets.UTF_8);
            log.debug("State decode edildi: {}", decoded);

            if (!decoded.contains("therapistId=")) {
                throw new IllegalArgumentException("State parameter'ında therapistId bulunamadı");
            }

            String idStr = decoded.split("therapistId=")[1];
            if (idStr.contains("&")) {
                idStr = idStr.split("&")[0];
            }

            Long therapistId = Long.parseLong(idStr.trim());

            if (therapistId <= 0) {
                throw new IllegalArgumentException("Geçersiz therapistId: " + therapistId);
            }

            return therapistId;

        } catch (NumberFormatException e) {
            log.error("TherapistId parse edilemedi: {}", state);
            throw new IllegalArgumentException("Geçersiz therapistId formatı: " + state, e);
        } catch (Exception e) {
            log.error("State parametresi işlenirken hata: {}", state, e);
            throw new IllegalArgumentException("State parametresi işlenemedi: " + state, e);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
                "service", "Google Calendar Integration",
                "status", "active",
                "message", "Servis çalışıyor"
        ));
    }
}