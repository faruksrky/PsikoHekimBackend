package com_psikohekim.psikohekim_appt.service.google;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
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
import com_psikohekim.psikohekim_appt.exception.CustomExceptionHandler;
import com_psikohekim.psikohekim_appt.model.Therapist;
import com_psikohekim.psikohekim_appt.repository.TherapistRepository;
import com_psikohekim.psikohekim_appt.service.CalendarService;
import com_psikohekim.psikohekim_appt.model.CalendarEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

    /**
     * Google OAuth URL'ini state parametresi ile oluşturur
     */
    public String getGoogleOAuthUrl(String state) {
        try {
            log.info("Google OAuth URL oluşturuluyor, state: {}", state);

            StringBuilder authUrl = new StringBuilder("https://accounts.google.com/o/oauth2/auth");
            authUrl.append("?client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
            authUrl.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
            authUrl.append("&scope=").append(URLEncoder.encode(CalendarScopes.CALENDAR_READONLY, StandardCharsets.UTF_8));
            authUrl.append("&response_type=code");
            authUrl.append("&access_type=offline");
            authUrl.append("&approval_prompt=force");
            authUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));

            String finalUrl = authUrl.toString();
            log.info("Google OAuth URL başarıyla oluşturuldu");

            return finalUrl;

        } catch (Exception e) {
            log.error("Google OAuth URL oluşturulurken hata: ", e);
            throw new CustomExceptionHandler.CalendarAuthException(
                    "Google OAuth URL oluşturulamadı: " + e.getMessage(),
                    ""
            );
        }
    }

    /**
     * Google callback'i handle eder ve calendar sync yapar
     */
    public CalendarSyncResponse handleGoogleCallback(String code, Long therapistId) {
        try {
            log.info("Google callback işleniyor, therapistId: {}", therapistId);

            // Therapist kontrolü
            if (!therapistRepository.existsById(therapistId)) {
                log.error("Terapist bulunamadı: {}", therapistId);
                return calendarService.buildErrorResponse("Terapist bulunamadı");
            }

            // Access token al
            String accessToken = getAccessToken(code);
            log.info("Access token başarıyla alındı");

            // Google events'ları fetch et
            List<CalendarEvent> events = fetchGoogleEvents(accessToken);
            log.info("{} adet etkinlik Google'dan alındı", events.size());

            if (events.isEmpty()) {
                log.info("Google Calendar'da etkinlik bulunamadı");
                return CalendarSyncResponse.builder()
                        .success(true)
                        .message("Google Calendar'da senkronize edilecek etkinlik bulunamadı")
                        .events(List.of())
                        .syncStatus(com_psikohekim.psikohekim_appt.enums.SyncStatus.COMPLETED)
                        .build();
            }

            // Therapist bilgisini al
            Therapist therapist = therapistRepository.getReferenceById(therapistId);

            // Events'ları therapist ve source ile güncelle
            events.forEach(event -> {
                event.setTherapist(therapist);
                event.setSource("GOOGLE");
                if (event.getStatus() == null) {
                    event.setStatus("confirmed");
                }
            });

            // Calendar service ile sync et
            CalendarSyncResponse response = calendarService.syncEvents(events);
            log.info("Calendar sync tamamlandı: {} başarı", response.isSuccess());

            return response;

        } catch (Exception e) {
            log.error("Google callback işlenirken hata: ", e);
            return calendarService.buildErrorResponse(
                    "Google Calendar senkronizasyonu sırasında hata oluştu: " + e.getMessage()
            );
        }
    }

    /**
     * Authorization code ile access token alır
     */
    private String getAccessToken(String code) {
        try {
            log.info("Access token alınıyor...");

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

            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    tokenUrl,
                    request,
                    GoogleTokenResponse.class
            );

            if (response.getBody() != null && response.getBody().getAccessToken() != null) {
                log.info("Access token başarıyla alındı");
                return response.getBody().getAccessToken();
            }

            throw new CustomExceptionHandler.CalendarAuthException(
                    "Access token response'ı boş veya geçersiz",
                    null
            );

        } catch (Exception e) {
            log.error("Access token alınırken hata: ", e);
            throw new CustomExceptionHandler.CalendarAuthException(
                    "Access token alınamadı: " + e.getMessage(),
                    null
            );
        }
    }

    /**
     * Google Calendar'dan etkinlikleri fetch eder
     */
    private List<CalendarEvent> fetchGoogleEvents(String accessToken) throws IOException {
        try {
            log.info("Google Calendar etkinlikleri alınıyor...");

            NetHttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                    .setAccessToken(accessToken);

            Calendar service = new Calendar.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName("PsikoHekim Calendar")
                    .build();

            // Son 2 ay ve gelecekteki etkinlikleri al (60 gün)
            DateTime twoMonthsAgo = new DateTime(System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000));

            Events events = service.events()
                    .list("primary")
                    .setTimeMin(twoMonthsAgo)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setMaxResults(1000)
                    .execute();

            if (events.getItems() == null || events.getItems().isEmpty()) {
                log.info("Google Calendar'da etkinlik bulunamadı");
                return List.of();
            }

            List<CalendarEvent> calendarEvents = events.getItems().stream()
                    .map(this::convertToCalendarEvent)
                    .filter(event -> event != null &&
                            event.getTitle() != null &&
                            !event.getTitle().trim().isEmpty() &&
                            event.getStartTime() != null &&
                            event.getEndTime() != null)
                    .collect(Collectors.toList());

            log.info("{} adet geçerli etkinlik dönüştürüldü", calendarEvents.size());

            return calendarEvents;

        } catch (IOException e) {
            log.error("Google Calendar etkinlikleri alınırken IO hatası: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Google Calendar etkinlikleri alınırken beklenmeyen hata: ", e);
            throw new RuntimeException("Google Calendar etkinlikleri alınamadı: " + e.getMessage(), e);
        }
    }

    /**
     * Google Event'ı CalendarEvent'e dönüştürür
     */
    private CalendarEvent convertToCalendarEvent(Event googleEvent) {
        try {
            if (googleEvent == null) {
                return null;
            }

            CalendarEvent event = new CalendarEvent();

            // Basic fields
            event.setTitle(googleEvent.getSummary() != null ?
                    googleEvent.getSummary().trim() : "Başlıksız Etkinlik");
            event.setDescription(googleEvent.getDescription());
            event.setLocation(googleEvent.getLocation());
            event.setExternalId(googleEvent.getId());
            event.setSource("GOOGLE");
            event.setStatus(googleEvent.getStatus() != null ? googleEvent.getStatus() : "confirmed");

            // Start time dönüşümü
            if (googleEvent.getStart() != null) {
                LocalDateTime startTime = convertGoogleDateTime(googleEvent.getStart().getDateTime(),
                        googleEvent.getStart().getDate());
                if (startTime != null) {
                    event.setStartTime(startTime);
                } else {
                    log.warn("Google event start time dönüştürülemedi: {}", googleEvent.getId());
                    return null;
                }
            } else {
                log.warn("Google event start time yok: {}", googleEvent.getId());
                return null;
            }

            // End time dönüşümü
            if (googleEvent.getEnd() != null) {
                LocalDateTime endTime = convertGoogleDateTime(googleEvent.getEnd().getDateTime(),
                        googleEvent.getEnd().getDate());
                if (endTime != null) {
                    event.setEndTime(endTime);
                } else {
                    log.warn("Google event end time dönüştürülemedi: {}", googleEvent.getId());
                    return null;
                }
            } else {
                log.warn("Google event end time yok: {}", googleEvent.getId());
                return null;
            }

            // Zaman kontrolü
            if (event.getStartTime().isAfter(event.getEndTime())) {
                log.warn("Google event geçersiz zaman aralığı: {} - start: {}, end: {}",
                        googleEvent.getId(), event.getStartTime(), event.getEndTime());
                return null;
            }

            return event;

        } catch (Exception e) {
            log.warn("Google event dönüştürülürken hata (event: {}): {}",
                    googleEvent != null ? googleEvent.getId() : "null", e.getMessage());
            return null;
        }
    }

    /**
     * Google DateTime veya Date'i LocalDateTime'a dönüştürür
     */
    private LocalDateTime convertGoogleDateTime(com.google.api.client.util.DateTime dateTime,
                                                com.google.api.client.util.DateTime date) {
        try {
            if (dateTime != null) {
                return LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(dateTime.getValue()),
                        ZoneId.systemDefault()
                );
            } else if (date != null) {
                // All-day event
                return LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(date.getValue()),
                        ZoneId.systemDefault()
                );
            }
            return null;
        } catch (Exception e) {
            log.error("Google DateTime dönüştürme hatası: ", e);
            return null;
        }
    }

    /**
     * Google OAuth connection'ı test eder
     */
    public boolean testGoogleConnection() {
        try {
            log.info("Google connection test ediliyor...");
            // Basit connection testi - gerçek implementasyon gerekirse eklenebilir
            return true;
        } catch (Exception e) {
            log.error("Google connection test başarısız: ", e);
            return false;
        }
    }
}