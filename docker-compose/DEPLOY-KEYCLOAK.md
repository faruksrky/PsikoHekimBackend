# Keycloak Kurulumu (Sadece Auth)

## 1. .env oluştur

```bash
cd ~/PsikoHekimBackend
cp .env.example .env
nano .env
```

En azından şunları doldur:
```
POSTGRES_PASSWORD=guclu_sifre_buraya
KEYCLOAK_ADMIN_PASSWORD=admin_sifresi_buraya
```

## 2. Eski container'ları durdur (varsa)

```bash
cd ~/PsikoHekimBackend
docker compose -f docker-compose/docker-compose.prod.yml down
```

## 3. Keycloak + PostgreSQL başlat

```bash
docker compose -f docker-compose/docker-compose.keycloak.yml up -d
```

## 4. Başlamasını bekle (1-2 dakika)

```bash
docker logs -f psikohekim-keycloak
```

"Listening on: http://0.0.0.0:8080" görünce Ctrl+C

## 5. Nginx config (zaten varsa atla)

```bash
sudo cp ~/PsikoHekimBackend/nginx/auth.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/auth.iyihislerapp.com
sudo ln -sf /etc/nginx/sites-available/auth.iyihislerapp.com /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

## 6. Test

https://auth.iyihislerapp.com/admin/master/console/

Admin: KEYCLOAK_ADMIN (varsayılan: admin)
Şifre: KEYCLOAK_ADMIN_PASSWORD
