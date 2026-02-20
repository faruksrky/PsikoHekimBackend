# PsikoHekim - Docker Compose

Keycloak ayrı projede (Desktop/Keycloak). Bu repo sadece PsikoHekim backend ve BPMN.

## Projeler

| Proje | Klasör | Açıklama |
|-------|--------|----------|
| PsikoHekim Backend | `2-psikohekim/` | PostgreSQL, Redis |
| BPMN | `3-bpmn/` | Elasticsearch, Zeebe |

## PsikoHekim Backend

```bash
cd PsikoHekimBackend
cp .env.example .env
# .env: POSTGRES_PASSWORD, REDIS_PASSWORD, KEYCLOAK_ISSUER_URI doldur
./deploy.sh psikohekim
```

## Keycloak

Keycloak ayrı projede: `cd ~/Keycloak && docker compose up -d`

## Arşiv

Eski compose dosyaları `archive/` klasöründe.
