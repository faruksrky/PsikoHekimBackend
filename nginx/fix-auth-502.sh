#!/bin/bash
# 502 hatası için teşhis ve düzeltme
# Sunucuda: cd ~/PsikoHekimBackend/nginx && bash fix-auth-502.sh

set -e
cd "$(dirname "$0")"

echo "=== 1. Keycloak container ==="
docker ps | grep keycloak || echo "HATA: Keycloak container çalışmıyor!"

echo ""
echo "=== 2. Keycloak 8081 cevap veriyor mu? ==="
curl -sI http://127.0.0.1:8081/ 2>/dev/null | head -3 || echo "HATA: Keycloak 8081'e cevap vermiyor!"

echo ""
echo "=== 3. Nginx config dosyaları ==="
ls -la /etc/nginx/sites-enabled/ 2>/dev/null || ls -la /etc/nginx/conf.d/ 2>/dev/null || echo "Nginx config dizini bulunamadı"

echo ""
echo "=== 4. auth.iyihislerapp.com config var mı? ==="
grep -l "auth.iyihislerapp" /etc/nginx/sites-enabled/* 2>/dev/null || \
grep -l "auth.iyihislerapp" /etc/nginx/sites-available/* 2>/dev/null || \
grep -l "auth.iyihislerapp" /etc/nginx/conf.d/* 2>/dev/null || \
echo "HATA: auth config bulunamadı!"

echo ""
echo "=== 5. Nginx kurulumu yapılıyor... ==="
sudo cp auth.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/auth.iyihislerapp.com
sudo cp api.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/api.iyihislerapp.com
sudo ln -sf /etc/nginx/sites-available/auth.iyihislerapp.com /etc/nginx/sites-enabled/
sudo ln -sf /etc/nginx/sites-available/api.iyihislerapp.com /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default 2>/dev/null || true

echo ""
echo "=== 6. Nginx test ==="
sudo nginx -t

echo ""
echo "=== 7. Nginx reload ==="
sudo systemctl reload nginx

echo ""
echo "=== Tamamlandı. Test et: https://auth.iyihislerapp.com/admin/master/console/ ==="
