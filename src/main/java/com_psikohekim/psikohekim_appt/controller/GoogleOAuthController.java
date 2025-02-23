package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.service.google.GoogleCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@RestController
@RequestMapping("/api/google-calendar")
@RequiredArgsConstructor
public class GoogleOAuthController {

    private final GoogleCalendarService googleCalendarService;

    @GetMapping("/redirect-to-google-oauth")
    public ResponseEntity<Void> redirectToGoogleOAuth() {
        String authUrl = googleCalendarService.getGoogleOAuthUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", authUrl);
        return ResponseEntity.status(302).headers(headers).build(); // ✅ 302 Redirect
    }

    /**
     * ✅ Google Callback URL - GET ve POST metodlarını destekler
     */
    @RequestMapping(value = "/callback", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<Map<String, Object>> handleGoogleOAuthCallback(@RequestParam Map<String, String> requestParams) {
        try {
            String authCode = requestParams.get("code");
            if (authCode == null || authCode.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Yetkilendirme kodu eksik"));
            }
            // Fetch the access token
            Map<String, Object> tokenResponse = googleCalendarService.fetchGoogleAccessToken(authCode);

            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            System.err.println("Callback Error:" + e.getMessage()); // Log any errors
            return ResponseEntity.internalServerError().body(Map.of("error", "Token alma hatası: " + e.getMessage()));
        }
    }

    @GetMapping("/fetch-google-calendar-events")
    public ResponseEntity<Map<String, Object>> fetchGoogleCalendarEvents(@RequestParam("accessToken") String accessToken) {
        try {
            Map<String, Object> events = googleCalendarService.fetchGoogleCalendarEvents(accessToken);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Etkinlikler çekilemedi: " + e.getMessage()));
        }
    }
}