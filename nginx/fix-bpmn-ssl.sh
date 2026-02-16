#!/bin/bash
# BPMN için SSL ve routing düzeltmesi
# Sunucuda: sudo bash fix-bpmn-ssl.sh

set -e

echo "=== 1. bpmn config'ini etkinleştir ==="
sudo mv /etc/nginx/sites-enabled/bpmn.iyihislerapp.com.disabled /etc/nginx/sites-enabled/bpmn.iyihislerapp.com 2>/dev/null || true

echo "=== 2. bpmn config içeriğini kontrol et ==="
if ! grep -q "listen 443 ssl" /etc/nginx/sites-available/bpmn.iyihislerapp.com 2>/dev/null; then
  echo "SSL bloğu ekleniyor..."
  sudo tee -a /etc/nginx/sites-available/bpmn.iyihislerapp.com << 'SSLEOF'

server {
    listen 443 ssl;
    server_name bpmn.iyihislerapp.com;
    ssl_certificate /etc/letsencrypt/live/bpmn.iyihislerapp.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/bpmn.iyihislerapp.com/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;

    location / {
        proxy_pass http://127.0.0.1:8082;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
SSLEOF
else
  echo "SSL bloğu zaten var"
fi

echo "=== 3. z-cloudflare-default devre dışı (bpmn artık doğrudan) ==="
sudo mv /etc/nginx/sites-enabled/z-cloudflare-default /etc/nginx/sites-enabled/z-cloudflare-default.disabled 2>/dev/null || true

echo "=== 4. api ve auth etkin mi kontrol et ==="
sudo mv /etc/nginx/sites-enabled/api.iyihislerapp.com.disabled /etc/nginx/sites-enabled/api.iyihislerapp.com 2>/dev/null || true
sudo mv /etc/nginx/sites-enabled/auth.iyihislerapp.com.disabled /etc/nginx/sites-enabled/auth.iyihislerapp.com 2>/dev/null || true

echo "=== 5. Nginx test ve reload ==="
sudo nginx -t && sudo systemctl reload nginx

echo ""
echo "=== Test ==="
echo "curl -sk https://bpmn.iyihislerapp.com/actuator/health"
echo "Beklenen: {\"status\":\"UP\"}"
