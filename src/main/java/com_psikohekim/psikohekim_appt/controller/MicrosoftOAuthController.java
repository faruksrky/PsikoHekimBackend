package com_psikohekim.psikohekim_appt.controller;

import com_psikohekim.psikohekim_appt.service.outlook.MicrosoftOAuthService;
import com.microsoft.graph.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/microsoft")
@Slf4j
public class MicrosoftOAuthController {

    private final MicrosoftOAuthService microsoftOAuthService;

    public MicrosoftOAuthController(MicrosoftOAuthService microsoftOAuthService) {
        this.microsoftOAuthService = microsoftOAuthService;
    }

    /**
     * Microsoft OAuth login başlat
     */
    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> startLogin() {
        try {
            log.info("🚀 Microsoft OAuth login başlatılıyor...");
            
            String state = UUID.randomUUID().toString();
            String authorizationUrl = microsoftOAuthService.generateAuthorizationUrl(state);
            
            Map<String, String> response = new HashMap<>();
            response.put("authorizationUrl", authorizationUrl);
            response.put("state", state);
            
            log.info("✅ Microsoft OAuth login URL oluşturuldu");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Microsoft OAuth login başlatma hatası: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Microsoft OAuth callback
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, String>> handleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error) {
        
        try {
            log.info("🔄 Microsoft OAuth callback işleniyor...");
            
            if (error != null) {
                log.error("❌ Microsoft OAuth error: {}", error);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", error);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Access token al
            String accessToken = microsoftOAuthService.getAccessToken(code);
            
            // User bilgilerini al
            User user = microsoftOAuthService.getUserInfo(accessToken);
            
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("userEmail", user.mail);
            response.put("userName", user.displayName);
            response.put("message", "Microsoft OAuth başarılı!");
            
            log.info("✅ Microsoft OAuth callback başarılı: {}", user.displayName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Microsoft OAuth callback hatası: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "OAuth callback hatası: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Token validate et
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        try {
            String accessToken = request.get("accessToken");
            
            boolean isValid = microsoftOAuthService.validateToken(accessToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "Token geçerli" : "Token geçersiz");
            
            log.info("🔍 Token validation: {}", isValid);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Token validation hatası: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", "Token validation hatası: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Microsoft OAuth service çalışıyor!");
        response.put("status", "OK");
        
        return ResponseEntity.ok(response);
    }
}

