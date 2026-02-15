#!/bin/bash
# Cloudflare Proxy ile Nginx kurulumu
# Cloudflare Flexible SSL kullanır - sunucuda sertifika gerekmez
# Önce Cloudflare Dashboard'da DNS kayıtlarında proxy (turuncu bulut) açık olmalı

set -e

DOMAIN_AUTH="auth.iyihislerapp.com"
DOMAIN_API="api.iyihislerapp.com"

echo "=== Cloudflare Nginx Kurulumu ==="

# Cloudflare config'leri kopyala
sudo cp auth.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/auth.iyihislerapp.com
sudo cp api.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/api.iyihislerapp.com

# Eski SSL config varsa devre dışı bırak (opsiyonel)
# sudo rm -f /etc/nginx/sites-enabled/auth.iyihislerapp.com
# sudo rm -f /etc/nginx/sites-enabled/api.iyihislerapp.com

# Symlink'leri oluştur
sudo ln -sf /etc/nginx/sites-available/auth.iyihislerapp.com /etc/nginx/sites-enabled/
sudo ln -sf /etc/nginx/sites-available/api.iyihislerapp.com /etc/nginx/sites-enabled/

# Default site'ı devre dışı bırak (conflict olmasın)
sudo rm -f /etc/nginx/sites-enabled/default 2>/dev/null || true

# Nginx test ve reload
sudo nginx -t && sudo systemctl reload nginx

echo ""
echo "=== Kurulum tamamlandı ==="
echo "Cloudflare Dashboard kontrol listesi:"
echo "  1. DNS: auth.iyihislerapp.com ve api.iyihislerapp.com -> Proxy ON (turuncu bulut)"
echo "  2. SSL/TLS -> Overview -> Flexible"
echo "  3. SSL/TLS -> Edge Certificates -> Always Use HTTPS: ON"
echo ""
