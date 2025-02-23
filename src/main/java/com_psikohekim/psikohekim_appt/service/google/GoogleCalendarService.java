package com_psikohekim.psikohekim_appt.service.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.google.api.client.json.gson.GsonFactory;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
public class GoogleCalendarService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String authUrl;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUrl;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;



    public String getGoogleOAuthUrl() {
        String scope = URLEncoder.encode("https://www.googleapis.com/auth/calendar.readonly", StandardCharsets.UTF_8);
        String state = URLEncoder.encode("random_state_value", StandardCharsets.UTF_8); // CSRF koruma amaçlı

        return String.format(
                "%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&access_type=offline&include_granted_scopes=true&state=%s",
                authUrl, clientId, redirectUri, scope, state
        );
    }


  public Map<String, Object> fetchGoogleAccessToken(String authCode) {
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("code", authCode);
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("redirect_uri", redirectUri);
    params.add("grant_type", "authorization_code");

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    System.out.println("📌 Google Token İsteği Yapılıyor...");
    System.out.println("➡️ authCode: " + authCode);
    System.out.println("➡️ client_id: " + clientId);
    System.out.println("➡️ redirect_uri: " + redirectUri);

    // 📌 Google'dan gelen yanıtı ResponseEntity olarak al
    ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

    System.out.println("📌 Google Yanıt Kodu: " + response.getStatusCode());
    System.out.println("📌 Google Yanıtı: " + response.getBody());

    Map<String, Object> responseMap = new HashMap<>(); // responseMap burada tanımlanıyor

    if (response.getStatusCode().is2xxSuccessful()) {
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            for (Map.Entry<String, Object> entry : responseBody.entrySet()) {
                Object value = entry.getValue();
                responseMap.put(entry.getKey(), value); // Değeri doğrudan ekleyin
            }
        } else {
            throw new RuntimeException("Google'dan gelen yanıt boş!");
        }
    }

    return responseMap; // responseMap'i döndür
}

        public Map<String, Object> fetchGoogleCalendarEvents(String accessToken) {
            Map<String, Object> eventsMap = new HashMap<>();

            try {
                // Google API istemcisini oluştur
                final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

                Calendar calendar = new Calendar.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
                        .setApplicationName("PsikoHekimApp")
                        .build();

                // Takvim etkinliklerini çek
                Events events = calendar.events().list("primary")
                        .setMaxResults(10) // İlk 10 etkinliği çek
                        .execute();

                // Etkinlikleri Map'e ekle
                events.getItems().forEach(event -> {
                    eventsMap.put(event.getId(), event.getSummary());
                });

            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException("Google Takvim etkinlikleri çekilemedi: " + e.getMessage());
            }

            return eventsMap;
        }
    }
