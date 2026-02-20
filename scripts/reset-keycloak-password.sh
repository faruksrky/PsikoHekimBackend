#!/bin/bash
# Keycloak kullanıcı şifresi sıfırlama
# Kullanım: ./reset-keycloak-password.sh <username> <new_password>
# Örnek:   ./reset-keycloak-password.sh psikohekimofis@gmail.com PsikoHekim-Ofis28
#
# .env dosyasından KEYCLOAK_ADMIN ve KEYCLOAK_ADMIN_PASSWORD okunur

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$BACKEND_DIR"

if [ ! -f .env ]; then
  echo "HATA: .env dosyası bulunamadı"
  exit 1
fi

source .env 2>/dev/null || true

USERNAME="${1:?Kullanım: $0 <username> <new_password>}"
NEW_PASSWORD="${2:?Kullanım: $0 <username> <new_password>}"

ADMIN_USER="${KEYCLOAK_ADMIN:-admin}"
ADMIN_PASS="${KEYCLOAK_ADMIN_PASSWORD:?KEYCLOAK_ADMIN_PASSWORD .env'de tanımlı olmalı}"
REALM="psikohekim"
KEYCLOAK_URL="http://localhost:8080"

echo ">>> Admin token alınıyor..."
TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASS" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "HATA: Admin token alınamadı. KEYCLOAK_ADMIN ve KEYCLOAK_ADMIN_PASSWORD kontrol edin."
  exit 1
fi

echo ">>> Kullanıcı aranıyor: $USERNAME"
USER_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM/users?username=$USERNAME&exact=true" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

if [ -z "$USER_ID" ]; then
  echo "HATA: Kullanıcı bulunamadı: $USERNAME"
  echo "Keycloak Admin Console'dan kullanıcıyı ekleyin veya username'i kontrol edin."
  exit 1
fi

echo ">>> Şifre sıfırlanıyor (temporary: false)..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$KEYCLOAK_URL/admin/realms/$REALM/users/$USER_ID/reset-password" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"type\":\"password\",\"value\":\"$NEW_PASSWORD\",\"temporary\":false}")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
if [ "$HTTP_CODE" = "204" ] || [ "$HTTP_CODE" = "200" ]; then
  echo ">>> Başarılı! Şifre güncellendi."
  echo ">>> Giriş testi:"
  echo "curl -X POST \"https://auth.iyihislerapp.com/keycloak/getToken\" \\"
  echo "  -H \"Content-Type: application/json\" \\"
  echo "  -d '{\"username\":\"$USERNAME\",\"password\":\"***\"}'"
else
  echo "HATA: Şifre sıfırlanamadı (HTTP $HTTP_CODE)"
  echo "$RESPONSE" | head -n -1
  exit 1
fi
