# Cloudflare ile Kurulum

Keycloak production modunda Cloudflare proxy ile çalışır. Cloudflare SSL'i yönettiği için sunucuda Let's Encrypt sertifikası gerekmez.

## Akış

```
Kullanıcı --[HTTPS]--> Cloudflare --[HTTP:80]--> Nginx --[HTTP]--> Keycloak
```

- **Cloudflare Flexible SSL**: Kullanıcı Cloudflare'a HTTPS ile bağlanır, Cloudflare sunucumuza HTTP (port 80) ile bağlanır
- **X-Forwarded-Proto**: Cloudflare bu header'ı ekler, Keycloak doğru URL'leri üretir

## Cloudflare Dashboard Ayarları

### 1. DNS
- `auth.iyihislerapp.com` → VPS IP (187.77.77.215)
- `api.iyihislerapp.com` → VPS IP
- **Proxy status: Proxied (turuncu bulut)** – mutlaka açık olmalı

### 2. SSL/TLS
- **Overview** → **Flexible** (Cloudflare↔origin HTTP)
- **Edge Certificates** → **Always Use HTTPS**: ON

### 3. (Opsiyonel) Güvenlik
- **Settings** → **Security Level**: Medium
- **Bot Fight Mode**: Açık (spam azaltır)

## VPS Kurulumu

```bash
cd /path/to/PsikoHekimBackend/nginx
chmod +x setup-cloudflare.sh
./setup-cloudflare.sh
```

Veya manuel:

```bash
sudo cp auth.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/auth.iyihislerapp.com
sudo cp api.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/api.iyihislerapp.com
sudo ln -sf /etc/nginx/sites-available/auth.iyihislerapp.com /etc/nginx/sites-enabled/
sudo ln -sf /etc/nginx/sites-available/api.iyihislerapp.com /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

## Keycloak Ortam Değişkenleri

`docker-compose.prod.yml` içinde Keycloak için:

- `KC_PROXY: edge` ✓
- `KC_HOSTNAME: auth.iyihislerapp.com` ✓
- `KC_HOSTNAME_PORT: 443` ✓ (kullanıcı tarafında HTTPS)
- `KC_HOSTNAME_STRICT_HTTPS: false` ✓

## Sorun Giderme

**502 Bad Gateway:**
- Keycloak container çalışıyor mu: `docker ps`
- Nginx log: `sudo tail -f /var/log/nginx/error.log`
- Keycloak log: `docker logs psikohekim-keycloak`

**Yanlış redirect URL:**
- Cloudflare proxy (turuncu bulut) açık mı kontrol et
- X-Forwarded-Proto header'ı geliyor mu: `curl -I http://localhost -H "Host: auth.iyihislerapp.com" -H "X-Forwarded-Proto: https"`
