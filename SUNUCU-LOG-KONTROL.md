# Sunucu Log Kontrol Komutları

## Backend (PsikoHekim) Logları

```bash
# Docker container adı ile
docker logs psikohekim-backend --tail 200 -f

# veya docker-compose ile
docker compose -f docker-compose/2-psikohekim/docker-compose.yml logs -f psikohekim-backend --tail 200

# Son 500 satır
docker logs psikohekim-backend --tail 500

# Hata satırlarını filtrele
docker logs psikohekim-backend 2>&1 | grep -i error
docker logs psikohekim-backend 2>&1 | grep -i 403
docker logs psikohekim-backend 2>&1 | grep -i BPMN
```

## BPMN Logları

```bash
# BPMN container adını bul
docker ps | grep -i bpmn

# Logları izle
docker logs BPMN_CONTAINER_ADI --tail 200 -f
```

## Nginx Logları

```bash
# Access log
sudo tail -f /var/log/nginx/access.log

# Error log
sudo tail -f /var/log/nginx/error.log

# 403 hatalarını filtrele
sudo grep 403 /var/log/nginx/access.log | tail -20
```

## Sistem Logları

```bash
# journalctl ile servis logları
sudo journalctl -u nginx -f --no-pager
```
