# PsikoHekim - Docker Compose (Proje Bazlı)

3 proje ayrı ayrı ayağa kaldırılır. Her proje kendi gereksinimleriyle çalışır.

## Proje Sırası

| # | Proje | Klasör | Gereksinimler |
|---|-------|--------|---------------|
| 1 | Keycloak | `1-keycloak/` | PostgreSQL |
| 2 | PsikoHekim Backend | `2-psikohekim/` | PostgreSQL, Redis, Keycloak URL |
| 3 | BPMN | `3-bpmn/` | Elasticsearch, Zeebe |

## Proje 1: Keycloak

```bash
cd PsikoHekimBackend
cp .env.example .env   # .env oluştur, POSTGRES_PASSWORD ve KEYCLOAK_ADMIN_PASSWORD doldur
docker compose -f docker-compose/1-keycloak/docker-compose.yml --env-file .env up -d
```

- **PostgreSQL** + **Keycloak** başlar
- Keycloak: http://localhost:8080
- Admin: KEYCLOAK_ADMIN / KEYCLOAK_ADMIN_PASSWORD

## Proje 2: PsikoHekim

Gereksinimler eklenecek (PostgreSQL, Redis, Keycloak URL).

## Proje 3: BPMN

Gereksinimler eklenecek (Elasticsearch, Zeebe).

## Arşiv

Eski monolitik compose dosyaları `archive/` klasöründe.
