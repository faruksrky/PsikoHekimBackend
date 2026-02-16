#!/bin/bash
# PsikoHekim - Nginx + Let's Encrypt SSL Kurulumu
# ÖNCE: auth.iyihislerapp.com ve api.iyihislerapp.com DNS'te 187.77.77.215'e yönlendir
# Kullanım: sudo bash setup-ssl.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo ">>> Nginx ve Certbot kuruluyor..."
apt update
apt install -y nginx certbot python3-certbot-nginx

echo ">>> Geçici HTTP config (sertifika doğrulaması için)..."
cat > /etc/nginx/sites-available/iyihislerapp << 'HTTPEOF'
server {
    listen 80;
    server_name auth.iyihislerapp.com;
    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
server {
    listen 80;
    server_name api.iyihislerapp.com;
    location / {
        proxy_pass http://127.0.0.1:8084;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
HTTPEOF

ln -sf /etc/nginx/sites-available/iyihislerapp /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default 2>/dev/null || true
nginx -t && systemctl reload nginx

echo ">>> Let's Encrypt sertifikaları alınıyor..."
CERT_EMAIL="${1:-admin@iyihislerapp.com}"
certbot --nginx -d auth.iyihislerapp.com -d api.iyihislerapp.com --non-interactive --agree-tos --email "$CERT_EMAIL"

echo ">>> SSL config'ler uygulanıyor..."
cp "$SCRIPT_DIR/auth.iyihislerapp.com.conf" /etc/nginx/sites-available/
cp "$SCRIPT_DIR/api.iyihislerapp.com.conf" /etc/nginx/sites-available/
rm /etc/nginx/sites-enabled/iyihislerapp
ln -sf /etc/nginx/sites-available/auth.iyihislerapp.com.conf /etc/nginx/sites-enabled/
ln -sf /etc/nginx/sites-available/api.iyihislerapp.com.conf /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx

echo ""
echo ">>> Tamamlandı!"
echo "   Keycloak: https://auth.iyihislerapp.com"
echo "   Backend:  https://api.iyihislerapp.com"
echo ""
echo ">>> .env güncelle ve backend'i yeniden başlat:"
echo "   KEYCLOAK_ISSUER_URI=https://auth.iyihislerapp.com/realms/psikohekim"
echo "   KEYCLOAK_HOSTNAME=auth.iyihislerapp.com"
echo "   WEBHOOK_BASE_URL=https://api.iyihislerapp.com"
echo "   GOOGLE_REDIRECT_URI=https://api.iyihislerapp.com/api/google-calendar/callback"
