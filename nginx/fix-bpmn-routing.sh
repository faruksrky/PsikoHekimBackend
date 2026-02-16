#!/bin/bash
# bpmn.iyihislerapp.com -> 8082 (BPMN API)
# Sorun: bpmn istekleri auth/api'ye gidiyordu
# Sunucuda: cd ~/PsikoHekimBackend/nginx && sudo bash fix-bpmn-routing.sh

set -e
cd "$(dirname "$0")"

echo "=== 1. Mevcut server_name'ler ==="
for f in /etc/nginx/sites-enabled/*; do
  echo "--- $f ---"
  grep "server_name" "$f" 2>/dev/null || true
done

echo ""
echo "=== 2. auth/api config'lerden bpmn kaldır ==="
for f in /etc/nginx/sites-enabled/auth* /etc/nginx/sites-enabled/api* 2>/dev/null; do
  [ -f "$f" ] || continue
  if grep -q "bpmn.iyihislerapp.com" "$f" 2>/dev/null; then
    echo "bpmn bulundu: $f - düzeltiliyor..."
    sudo sed -i.bak 's/ bpmn.iyihislerapp.com//g' "$f"
    sudo sed -i.bak 's/bpmn.iyihislerapp.com //g' "$f"
  fi
done

echo ""
echo "=== 3. BPMN config uygula ==="
sudo cp bpmn.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/bpmn.iyihislerapp.com 2>/dev/null || {
  echo "bpmn.iyihislerapp.com.cloudflare.conf yok - manuel oluşturuluyor..."
  sudo tee /etc/nginx/sites-available/bpmn.iyihislerapp.com << 'EOF'
server {
    listen 80;
    server_name bpmn.iyihislerapp.com;

    location / {
        proxy_pass http://127.0.0.1:8082;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $http_x_forwarded_proto;
    }
}
EOF
}
sudo ln -sf /etc/nginx/sites-available/bpmn.iyihislerapp.com /etc/nginx/sites-enabled/

echo ""
echo "=== 4. default_server kontrolü ==="
grep -r "default_server" /etc/nginx/sites-enabled/ 2>/dev/null || echo "default_server yok"

echo ""
echo "=== 5. Nginx test ve reload ==="
sudo nginx -t && sudo systemctl reload nginx

echo ""
echo "=== Tamamlandı. Test: curl -s -H 'Host: bpmn.iyihislerapp.com' http://127.0.0.1/actuator/health ==="
