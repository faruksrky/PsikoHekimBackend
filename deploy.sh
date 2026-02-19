#!/bin/bash
# PsikoHekim - Proje Bazlı Deploy
# Her proje ayrı ayağa kaldırılır.
#
# Proje 1 (Keycloak): ./deploy.sh keycloak
# Proje 2 (PsikoHekim): ./deploy.sh psikohekim
# Proje 3 (BPMN): ./deploy.sh bpmn  (henüz hazır değil)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PROJECT="${1:-keycloak}"

case "$PROJECT" in
  keycloak)
    if [ ! -f .env ]; then
      echo "HATA: .env dosyası yok. Önce: cp .env.example .env"
      echo "Sonra POSTGRES_PASSWORD ve KEYCLOAK_ADMIN_PASSWORD doldur."
      exit 1
    fi
    echo ">>> Proje 1: Keycloak başlatılıyor..."
    # Önce mevcut container'ları kaldır (isim çakışması önlenir)
    docker compose -f docker-compose/1-keycloak/docker-compose.yml --env-file .env down 2>/dev/null || true
    # Eski compose'dan kalan container varsa zorla kaldır
    docker rm -f keycloak-postgres keycloak 2>/dev/null || true
    docker compose -f docker-compose/1-keycloak/docker-compose.yml --env-file .env up -d
    echo ""
    echo ">>> Keycloak: http://localhost:8080"
    echo ">>> Log: docker compose -f docker-compose/1-keycloak/docker-compose.yml logs -f keycloak"
    ;;
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
    echo "Kullanım: ./deploy.sh [keycloak|psikohekim|bpmn]"
    echo "  keycloak  - Keycloak + PostgreSQL (varsayılan)"
    echo "  psikohekim - Backend (PostgreSQL, Redis)"
    echo "  bpmn     - BPMN servisleri (henüz hazır değil)"
    exit 1
    ;;
esac
