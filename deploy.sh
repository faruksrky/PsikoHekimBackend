#!/bin/bash
# PsikoHekim Backend - VPS Deploy Script
# Kullanım: ./deploy.sh
# Önce: .env dosyasını oluştur (cp .env.example .env)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f .env ]; then
  echo "HATA: .env dosyası yok. Önce: cp .env.example .env"
  echo "Sonra .env içindeki değerleri doldur."
  exit 1
fi

echo ">>> Docker Compose ile build ve başlatılıyor..."
docker compose -f docker-compose/docker-compose.prod.yml --env-file "$SCRIPT_DIR/.env" up -d --build

echo ""
echo ">>> Servisler başlatıldı. Durum:"
docker compose -f docker-compose/docker-compose.prod.yml --env-file "$SCRIPT_DIR/.env" ps

echo ""
echo ">>> Logları izlemek için:"
echo "   docker compose -f docker-compose/docker-compose.prod.yml logs -f backend"
echo ""
echo ">>> Durdurmak için:"
echo "   docker compose -f docker-compose/docker-compose.prod.yml down"
