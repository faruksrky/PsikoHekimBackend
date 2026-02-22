package com_psikohekim.psikohekim_appt.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * JWT token'dan rol bilgisi çıkarır.
 * Keycloak format: resource_access.DN.roles veya realm_access.roles
 */
public final class JwtUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JwtUtils() {
    }

    /**
     * Authorization header'dan (Bearer token) isAdmin bilgisini çıkarır.
     *
     * @param authHeader "Bearer eyJ..." formatında veya null
     * @return Admin rolü varsa true, yoksa false
     */
    public static boolean isAdminFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return false;
        }
        return isAdminFromToken(token);
    }

    /**
     * JWT token payload'ından Admin rolünü kontrol eder.
     */
    public static boolean isAdminFromToken(String token) {
        try {
            List<String> roles = extractRoles(token);
            return roles.stream()
                    .anyMatch(r -> "Admin".equalsIgnoreCase(r) || "ADMIN".equals(r));
        } catch (Exception e) {
            return false;
        }
    }

    private static List<String> extractRoles(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return List.of();
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode root = OBJECT_MAPPER.readTree(payload);

            // resource_access.<client>.roles (Keycloak client roles - DN, psikohekim vb.)
            JsonNode resourceAccess = root.path("resource_access");
            if (resourceAccess.isObject()) {
                var iter = resourceAccess.fields();
                while (iter.hasNext()) {
                    JsonNode client = iter.next().getValue();
                    JsonNode roles = client.path("roles");
                    if (roles.isArray()) {
                        List<String> list = new ArrayList<>();
                        roles.forEach(r -> list.add(r.asText()));
                        if (!list.isEmpty()) {
                            return list;
                        }
                    }
                }
            }

            // realm_access.roles
            JsonNode realmAccess = root.path("realm_access");
            if (realmAccess.isObject()) {
                JsonNode roles = realmAccess.path("roles");
                if (roles.isArray()) {
                    List<String> list = new ArrayList<>();
                    roles.forEach(r -> list.add(r.asText()));
                    return list;
                }
            }

            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }
}
