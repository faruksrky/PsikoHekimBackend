package com_psikohekim.psikohekim_appt.service.google;


import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com_psikohekim.psikohekim_appt.dto.response.CalendarSyncResponse;
import com_psikohekim.psikohekim_appt.dto.response.GoogleTokenResponse;
import com_psikohekim.psikohekim_appt.enums.SyncStatus;
import com_psikohekim.psikohekim_appt.exception.CustomExceptionHandler;
import com_psikohekim.psikohekim_appt.model.CustomUserDetails;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.repository.TherapistRepository;
import com_psikohekim.psikohekim_appt.service.CalendarService;
import com_psikohekim.psikohekim_appt.model.CalendarEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoogleCalendarIntegrationService {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final CalendarService calendarService;
    private final TherapistRepository therapistRepository;

    public String getGoogleOAuthUrl() {
        try {
            List<String> scopes = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    jsonFactory,
                    clientId,
                    clientSecret,
                    scopes)
                    .setAccessType("offline")
                    .build();

            return flow.newAuthorizationUrl()
                    .setRedirectUri(redirectUri)
                    .setApprovalPrompt("force")
                    .build();
        } catch (Exception e) {
            throw new CustomExceptionHandler.CalendarAuthException("Google OAuth URL oluşturulamadı", "");
        }
    }

        public CalendarSyncResponse handleGoogleCallback(String code, Long therapistId) {
            try {
                String accessToken = getAccessToken(code);
                List<CalendarEvent> events = fetchGoogleEvents(accessToken);

                Therapist therapist = therapistRepository.getReferenceById(therapistId);
                events.forEach(event -> {
                    event.setTherapist(therapist);
                    event.setSource("GOOGLE");
                });

                return calendarService.syncEvents(events);
            } catch (Exception e) {
                return calendarService.buildErrorResponse(e.getMessage());
            }
        }

    private String getAccessToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // Google TokenResponse yerine kendi sınıfımızı kullanalım
        ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                tokenUrl,
                request,
                GoogleTokenResponse.class
        );

        if (response.getBody() != null && response.getBody().getAccessToken() != null) {
            return response.getBody().getAccessToken();
        }

        throw new CustomExceptionHandler.CalendarAuthException("Access token alınamadı", null);
    }

    private List<CalendarEvent> fetchGoogleEvents(String accessToken) throws IOException {
        NetHttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(accessToken);

        Calendar service = new Calendar.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("PsikoHekim Calendar")
                .build();

        // Son 2 ay ve gelecekteki etkinlikleri al
        DateTime twoMonthsAgo = new DateTime(System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000));

        Events events = service.events()
                .list("primary")
                .setTimeMin(twoMonthsAgo)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        return events.getItems().stream()
                .map(this::convertToCalendarEvent)
                .collect(Collectors.toList());
    }

    private CalendarEvent convertToCalendarEvent(Event googleEvent) {
        CalendarEvent event = new CalendarEvent();
        event.setTitle(googleEvent.getSummary());
        event.setDescription(googleEvent.getDescription());
        event.setLocation(googleEvent.getLocation());
        event.setExternalId(googleEvent.getId());
        event.setSource("GOOGLE");
        event.setStatus(googleEvent.getStatus());

        // Zaman dönüşümleri
        if (googleEvent.getStart() != null) {
            if (googleEvent.getStart().getDateTime() != null) {
                event.setStartTime(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(googleEvent.getStart().getDateTime().getValue()),
                        ZoneId.systemDefault()
                ));
            } else if (googleEvent.getStart().getDate() != null) {
                event.setStartTime(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(googleEvent.getStart().getDate().getValue()),
                        ZoneId.systemDefault()
                ));
            }
        }

        if (googleEvent.getEnd() != null) {
            if (googleEvent.getEnd().getDateTime() != null) {
                event.setEndTime(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(googleEvent.getEnd().getDateTime().getValue()),
                        ZoneId.systemDefault()
                ));
            } else if (googleEvent.getEnd().getDate() != null) {
                event.setEndTime(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(googleEvent.getEnd().getDate().getValue()),
                        ZoneId.systemDefault()
                ));
            }
        }

        return event;
    }


}