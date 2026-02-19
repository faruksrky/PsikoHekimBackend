# API CORS Deploy Rehberi

## Sorun
api.iyihislerapp.com CORS hatası: /patient/all, /therapist/by-email, /process/inbox/pending

## Çözüm
api.iyihislerapp.com.cloudflare.conf - Port 80 ve 443 için CORS eklendi.

## Sunucuda Deploy

```bash
cd ~/PsikoHekimBackend
git pull
cd nginx

# API config (port 80 + 443 CORS)
sudo cp api.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/api.iyihislerapp.com

# SSL sertifikası api.iyihislerapp.com için yoksa, auth ile paylaşılıyor olabilir:
# Sunucuda kontrol et:
# ls /etc/letsencrypt/live/
# api.iyihislerapp.com yoksa, config'te ssl_certificate path'ini auth.iyihislerapp.com olarak değiştir

# Cloudflare default (eğer kullanılıyorsa)
sudo cp cloudflare-default.conf /etc/nginx/sites-available/z-cloudflare-default

sudo nginx -t && sudo systemctl reload nginx
```

## SSL Sertifika
api.iyihislerapp.com sertifikası yoksa:
```bash
# Config'te şu satırları değiştir:
# ssl_certificate /etc/letsencrypt/live/auth.iyihislerapp.com/fullchain.pem;
# ssl_certificate_key /etc/letsencrypt/live/auth.iyihislerapp.com/privkey.pem;
```

## Test
```bash
curl -X OPTIONS -H "Origin: https://psikohekimfrontend.pages.dev" \
  -H "Access-Control-Request-Method: GET" \
  -v https://api.iyihislerapp.com/patient/all
# Beklenen: Access-Control-Allow-Origin header
```
