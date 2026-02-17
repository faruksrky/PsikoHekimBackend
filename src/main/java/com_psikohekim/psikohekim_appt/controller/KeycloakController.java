package com_psikohekim.psikohekim_appt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Keycloak proxy - Frontend JSON format'ını Keycloak token endpoint'ine çevirir.
 * CORS Backend'de yapılandırıldığı için Cloudflare Pages'ten güvenli erişim sağlar.
 */
@RestController
@RequestMapping("/keycloak")
@RequiredArgsConstructor
@Slf4j
public class KeycloakController {

    private final RestTemplate restTemplate;

    @Value("${keycloak.auth-server-url:https://auth.iyihislerapp.com}")
    private String keycloakAuthServerUrl;

    private static final String REALM = "psikohekim";
    private static final String CLIENT_ID = "psikohekim-frontend";

    @PostMapping("/getToken")
    public ResponseEntity<?> getToken(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_request", "error_description", "username and password required"));
        }

        String tokenUrl = keycloakAuthServerUrl + "/realms/" + REALM + "/protocol/openid-connect/token";
        String formBody = "grant_type=password&client_id=" + CLIENT_ID
                + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>(formBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warn("Keycloak token error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            Object body = parseJsonOrString(e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).contentType(MediaType.APPLICATION_JSON).body(body);
        } catch (Exception e) {
            log.error("Keycloak token error: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/userInfo")
    public ResponseEntity<?> userInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userInfoUrl = keycloakAuthServerUrl + "/realms/" + REALM + "/protocol/openid-connect/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.warn("Keycloak userInfo error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            Object body = parseJsonOrString(e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).contentType(MediaType.APPLICATION_JSON).body(body);
        } catch (Exception e) {
            log.error("Keycloak userInfo error: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> body) {
        // Keycloak'ın update-password flow'u farklı - şimdilik 501
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of("error", "Not implemented"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        // Keycloak Admin API ile reset - şimdilik 501
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of("error", "Not implemented"));
    }

    private Object parseJsonOrString(String str) {
        if (str == null || str.isBlank()) return Map.of();
        try {
            return new ObjectMapper().readValue(str, Map.class);
        } catch (Exception e) {
            return str;
        }
    }
}
