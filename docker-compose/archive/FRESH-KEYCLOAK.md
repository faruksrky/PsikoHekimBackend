# Boş Keycloak Kurulumu

## 1. .env oluştur

```bash
cd ~/PsikoHekimBackend
nano .env
```

Şunları ekle (değerleri kendin belirle):
```
POSTGRES_PASSWORD=guclu_postgres_sifresi
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=guclu_admin_sifresi
```

## 2. Eski container'ları durdur

```bash
docker compose -f docker-compose/docker-compose.keycloak.yml down
docker compose -f docker-compose/docker-compose.prod.yml down 2>/dev/null
```

## 3. Başlat

```bash
docker compose -f docker-compose/docker-compose.keycloak-fresh.yml --env-file .env up -d
```

(postgres-init-fresh ile keycloak DB otomatik oluşur)

## 4. Başlamasını bekle (1-2 dakika)

```bash
docker logs -f keycloak
```
"Listening on: http://0.0.0.0:8080" görünce Ctrl+C

## 5. Giriş

https://auth.iyihislerapp.com/admin/master/console/

- Username: KEYCLOAK_ADMIN (.env'deki)
- Password: KEYCLOAK_ADMIN_PASSWORD (.env'deki)
