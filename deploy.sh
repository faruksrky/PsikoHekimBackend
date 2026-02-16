#!/bin/bash
# PsikoHekim - Proje Bazlı Deploy
# Her proje ayrı ayağa kaldırılır.
#
# Proje 1 (Keycloak): ./deploy.sh keycloak
# Proje 2 (PsikoHekim): ./deploy.sh psikohekim  (henüz hazır değil)
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
    docker compose -f docker-compose/1-keycloak/docker-compose.yml --env-file .env up -d
    echo ""
    echo ">>> Keycloak: http://localhost:8080"
    echo ">>> Log: docker compose -f docker-compose/1-keycloak/docker-compose.yml logs -f keycloak"
    ;;
  psikohekim)
    echo ">>> Proje 2: PsikoHekim - henüz hazır değil. Gereksinimler eklenince oluşturulacak."
    exit 1
    ;;
  bpmn)
    echo ">>> Proje 3: BPMN (Elasticsearch + Zeebe) başlatılıyor..."
    docker compose -f docker-compose/3-bpmn/docker-compose.yml up -d
    echo ""
    echo ">>> Zeebe: localhost:26500"
    echo ">>> Elasticsearch: localhost:9200"
    echo ">>> Log: docker compose -f docker-compose/3-bpmn/docker-compose.yml logs -f"
    ;;
  *)
    echo "Kullanım: ./deploy.sh [keycloak|psikohekim|bpmn]"
    echo "  keycloak  - Keycloak + PostgreSQL (varsayılan)"
    echo "  psikohekim - Backend (henüz hazır değil)"
    echo "  bpmn     - BPMN servisleri (henüz hazır değil)"
    exit 1
    ;;
esac
