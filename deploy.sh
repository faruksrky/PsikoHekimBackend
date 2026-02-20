#!/bin/bash
# PsikoHekim - Proje Bazlı Deploy
# Keycloak ayrı projede (Desktop/Keycloak). Bu script sadece PsikoHekim backend.
#
# ./deploy.sh psikohekim
# ./deploy.sh bpmn  (henüz hazır değil)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PROJECT="${1:-psikohekim}"

case "$PROJECT" in
  psikohekim)
    if [ ! -f .env ]; then
      echo "HATA: .env dosyası yok. Önce: cp .env.example .env"
      echo "Sonra POSTGRES_PASSWORD, REDIS_PASSWORD, KEYCLOAK_ISSUER_URI doldur."
      exit 1
    fi
    echo ">>> Proje 2: PsikoHekim Backend başlatılıyor..."
    docker compose -f docker-compose/2-psikohekim/docker-compose.yml --env-file .env up -d --build
    echo ""
    echo ">>> Backend: http://localhost:8083"
    echo ">>> Log: docker logs -f psikohekim-backend"
    ;;
  bpmn)
    echo ">>> Proje 3: BPMN - Ayrı proje olarak çalışır (BPMN repo)"
    echo ">>> BPMN projesinde: cd BPMN && docker compose up -d"
    echo ">>> BPMN API: localhost:8082"
    exit 1
    ;;
  *)
    echo "Kullanım: ./deploy.sh [psikohekim|bpmn]"
    echo "  psikohekim - Backend (PostgreSQL, Redis) - varsayılan"
    echo "  bpmn     - BPMN servisleri (henüz hazır değil)"
    echo ""
    echo "Keycloak ayrı projede: cd ~/Keycloak && docker compose up -d"
    exit 1
    ;;
esac
