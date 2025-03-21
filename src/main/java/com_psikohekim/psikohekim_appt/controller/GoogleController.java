package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.service.google.GoogleCalendarIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/google-calendar")
@RequiredArgsConstructor
public class GoogleController {
    private final GoogleCalendarIntegrationService googleCalendarService;

    @GetMapping("/redirect-to-google-oauth")
    public ResponseEntity<?> getGoogleAuthUrl(@RequestParam Long therapistId) {
        String authUrl = googleCalendarService.getGoogleOAuthUrl();
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(
            @RequestParam String code,
            @RequestParam(name = "state") String state  // state parametresini al
    ) {
        // state'den therapistId'yi çıkar
        Long therapistId = 1L; // Şimdilik sabit
        if (state != null && state.contains("therapistId=")) {
            String idStr = state.split("therapistId=")[1];
            therapistId = Long.parseLong(idStr);
        }

        return ResponseEntity.ok(googleCalendarService.handleGoogleCallback(code, therapistId));
    }
}