#!/bin/bash
# api.iyihislerapp.com -> Backend (8083) routing düzeltmesi
# Sorun: https://api.iyihislerapp.com Keycloak'a gidiyordu
# Sunucuda: cd ~/PsikoHekimBackend/nginx && sudo bash fix-api-ssl.sh

set -e

echo "=== 1. api config etkin mi? ==="
sudo ln -sf /etc/nginx/sites-available/api.iyihislerapp.com /etc/nginx/sites-enabled/api.iyihislerapp.com 2>/dev/null || true

echo "=== 2. api config içeriğini kontrol et (SSL bloğu) ==="
API_CONF="/etc/nginx/sites-available/api.iyihislerapp.com"
if ! grep -q "listen 443 ssl" "$API_CONF" 2>/dev/null; then
  echo "SSL bloğu ekleniyor..."
  # api için sertifika - auth ile paylaşılıyor olabilir
  CERT_PATH="/etc/letsencrypt/live/api.iyihislerapp.com"
  [ ! -d "$CERT_PATH" ] && CERT_PATH="/etc/letsencrypt/live/auth.iyihislerapp.com"
  sudo tee -a "$API_CONF" << EOF

server {
    listen 443 ssl;
    server_name api.iyihislerapp.com;
    ssl_certificate ${CERT_PATH}/fullchain.pem;
    ssl_certificate_key ${CERT_PATH}/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;

    location / {
        proxy_pass http://127.0.0.1:8083;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF
else
  echo "SSL bloğu zaten var"
fi

echo "=== 3. auth config'ten api kaldır (server_name) ==="
for f in /etc/nginx/sites-enabled/auth.iyihislerapp.com /etc/nginx/sites-available/auth.iyihislerapp.com 2>/dev/null; do
  [ -f "$f" ] && sudo sed -i.bak 's/server_name auth.iyihislerapp.com api.iyihislerapp.com/server_name auth.iyihislerapp.com/' "$f" 2>/dev/null || true
  [ -f "$f" ] && sudo sed -i.bak 's/server_name api.iyihislerapp.com auth.iyihislerapp.com/server_name auth.iyihislerapp.com/' "$f" 2>/dev/null || true
done

echo "=== 4. api config'ten auth kaldır (server_name) ==="
for f in /etc/nginx/sites-enabled/api.iyihislerapp.com /etc/nginx/sites-available/api.iyihislerapp.com 2>/dev/null; do
  [ -f "$f" ] && sudo sed -i.bak 's/server_name api.iyihislerapp.com auth.iyihislerapp.com/server_name api.iyihislerapp.com/' "$f" 2>/dev/null || true
  [ -f "$f" ] && sudo sed -i.bak 's/server_name auth.iyihislerapp.com api.iyihislerapp.com/server_name api.iyihislerapp.com/' "$f" 2>/dev/null || true
done

echo "=== 5. Nginx test ve reload ==="
sudo nginx -t && sudo systemctl reload nginx

echo ""
echo "=== Test ==="
echo "curl -sk https://api.iyihislerapp.com/actuator/health"
echo "Beklenen: {\"status\":\"UP\"}"
