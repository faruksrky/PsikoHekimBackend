# PsikoHekim Docker Compose

**Tek compose** – tüm servisler buradan yönetilir.

## Hızlı başlangıç

```bash
cd PsikoHekimBackend
docker compose -f docker-compose/docker-compose.yml --env-file .env up -d
```

Bu komut **postgres**, **redis** ve **keycloak** (realm import ile) servislerini başlatır.

## Profiller

| Komut | Servisler |
|-------|-----------|
| `docker compose -f docker-compose/docker-compose.yml up -d` | postgres, redis, keycloak |
| `docker compose -f docker-compose/docker-compose.yml --profile bpmn up -d` | + elasticsearch, zeebe |
| `docker compose -f docker-compose/docker-compose.yml --profile backend up -d` | + backend |

## Realm import

- **Konum:** `docker-compose/realm-import/psikohekim-realm.json`
- **İçerik:** psikohekim realm, psikohekim-frontend client, admin/user rolleri, admin kullanıcı (psikohekimofis@gmail.com)
- **İlk şifre:** `ChangeMe123!` (ilk girişte değiştir)

## Portlar (standart dışı - prod güvenliği)

| Servis | Port | Not |
|--------|------|-----|
| Postgres | 5433 | 5432 yerine |
| Redis | 6380 | 6379 yerine |
| Keycloak | 8081 | 8080 yerine |
| Backend | 8084 | 8083 yerine |
| Elasticsearch | 9201 | 9200 yerine |
| Zeebe | 26501, 9601 | |
