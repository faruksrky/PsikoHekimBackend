# 502 Bad Gateway - Kontrol Listesi

`/api/bpmn/patient/start-process` 502 dönüyorsa:

## 1. Backend çalışıyor mu?
```bash
curl -s http://127.0.0.1:8083/actuator/health
# veya
curl -s http://127.0.0.1:8083/patient/all
```

## 2. BPMN servisi erişilebilir mi? (Backend'den)
Backend, Feign ile `bpmn.iyihislerapp.com` veya `BPMN_SERVICE_URL` adresine istek atar.
```bash
# Sunucudan BPMN'ye erişim testi
curl -s -o /dev/null -w "%{http_code}" https://bpmn.iyihislerapp.com/api/bpmn/tasks?processInstanceId=test
```

## 3. Backend logları
```bash
# Docker ile çalışıyorsa
docker logs psikohekim-backend 2>&1 | tail -100

# systemd ile çalışıyorsa
journalctl -u psikohekim -n 100 --no-pager
```

## 4. Nginx config güncel mi?
```bash
cd ~/PsikoHekimBackend
git pull
sudo cp nginx/api.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/api.iyihislerapp.com
sudo nginx -t && sudo systemctl reload nginx
```

## 5. Backend yeniden başlat
```bash
./deploy.sh psikohekim
```
