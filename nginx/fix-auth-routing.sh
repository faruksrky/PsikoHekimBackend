#!/bin/bash
# auth.iyihislerapp.com -> 8080 (Keycloak), api.iyihislerapp.com -> 8083 (Backend)
# Sorun: auth istekleri api config'e gidiyordu
# Sunucuda: cd ~/PsikoHekimBackend/nginx && sudo bash fix-auth-routing.sh

set -e
cd "$(dirname "$0")"

echo "=== 1. Mevcut api config server_name ==="
grep "server_name" /etc/nginx/sites-enabled/api.iyihislerapp.com.conf 2>/dev/null || echo "api config bulunamadı"

echo ""
echo "=== 2. api config'te auth varsa kaldır ==="
sudo sed -i.bak 's/server_name api.iyihislerapp.com auth.iyihislerapp.com/server_name api.iyihislerapp.com/' /etc/nginx/sites-enabled/api.iyihislerapp.com.conf 2>/dev/null || true
sudo sed -i.bak 's/server_name auth.iyihislerapp.com api.iyihislerapp.com/server_name api.iyihislerapp.com/' /etc/nginx/sites-enabled/api.iyihislerapp.com.conf 2>/dev/null || true

echo ""
echo "=== 3. Auth Cloudflare config uygula ==="
sudo cp auth.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/auth.iyihislerapp.com
sudo ln -sf /etc/nginx/sites-available/auth.iyihislerapp.com /etc/nginx/sites-enabled/auth.iyihislerapp.com

echo ""
echo "=== 4. Nginx test ve reload ==="
sudo nginx -t && sudo systemctl reload nginx

echo ""
echo "=== Tamamlandı. Test: https://auth.iyihislerapp.com/admin/master/console/ ==="
