# BPMN CORS Düzeltmesi - Deploy Rehberi

## Sorun
`https://psikohekimfrontend.pages.dev` → `https://bpmn.iyihislerapp.com/api/bpmn/patient/start-process` isteği CORS nedeniyle engelleniyor.

## Kök Neden
bpmn.iyihislerapp.com **DNS only** (port 443, Let's Encrypt). Önceki config sadece port 80'de CORS vardı. OPTIONS preflight 443'e gidiyordu → Nginx proxy → BPMN backend "Invalid CORS request" 403 döndü.

## Çözüm
`bpmn.iyihislerapp.com.cloudflare.conf` dosyasına:
- Port 80: CORS (Cloudflare proxy için)
- **Port 443: CORS + OPTIONS handling** (DNS only için – asıl kullanılan)

---

## Deploy Adımları

### 1. Değişiklikleri Git'e push et (lokal makinede)
```bash
cd ~/Desktop/PsikoHekim/PsikoHekimBackend
git add nginx/bpmn.iyihislerapp.com.cloudflare.conf
git commit -m "BPMN: CORS header'ları eklendi (psikohekimfrontend.pages.dev)"
git push
```

### 2. Sunucuya bağlan ve güncelle
```bash
# deploy-remote.sh ile (sunucu adresini kendin yaz):
./deploy-remote.sh root@SUNUCU_IP

# VEYA manuel:
ssh root@SUNUCU_IP
cd ~/PsikoHekimBackend
git pull
```

### 3. Nginx config'i uygula (sunucuda)
```bash
cd ~/PsikoHekimBackend/nginx
sudo cp bpmn.iyihislerapp.com.cloudflare.conf /etc/nginx/sites-available/bpmn.iyihislerapp.com
sudo nginx -t && sudo systemctl reload nginx
```

**Not:** `fix-bpmn-routing.sh` da aynı copy işlemini yapar; ama sadece BPMN config güncellemek için yukarıdaki 3 satır yeterli.

---

## Test (Giriş/Çıkış Gerekmez)

### A) curl ile OPTIONS preflight testi (lokal makineden)
```bash
curl -X OPTIONS \
  -H "Origin: https://psikohekimfrontend.pages.dev" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type, Authorization" \
  -v https://bpmn.iyihislerapp.com/api/bpmn/patient/start-process
```

**Başarılı ise:** Response header'larda `Access-Control-Allow-Origin: https://psikohekimfrontend.pages.dev` görünmeli.

### B) Tarayıcıda test
1. https://psikohekimfrontend.pages.dev aç
2. Giriş yap (zaten yaptıysan sayfayı yenile)
3. Terapi oturumu oluştur/düzenle ekranında kaydet'e tıkla
4. CORS hatası olmamalı

---

## BPMN + Cloudflare Nasıl Çalışıyor?

| Servis | Cloudflare DNS | Trafik Akışı |
|--------|----------------|--------------|
| auth.iyihislerapp.com | Proxied (turuncu) | Tarayıcı → Cloudflare (HTTPS) → VPS (HTTP:80) → Nginx → 8080 |
| api.iyihislerapp.com | Proxied (turuncu) | Tarayıcı → Cloudflare (HTTPS) → VPS (HTTP:80) → Nginx → 8083 |
| bpmn.iyihislerapp.com | Proxied veya DNS only | Tarayıcı → (Cloudflare veya direkt) → VPS → Nginx → 8082 |

**Cloudflare Proxied (turuncu bulut):** İstek Cloudflare üzerinden geçer. SSL Cloudflare'da biter, origin'e HTTP gelir. Bu yüzden `bpmn.iyihislerapp.com.cloudflare.conf` port 80 dinler.

**Cloudflare DNS only (gri bulut):** İstek doğrudan VPS'e gider. Sunucuda Let's Encrypt SSL gerekir (443). Bu durumda farklı bir nginx config kullanılır.

Hangi modda olduğunu görmek için: Cloudflare Dashboard → DNS → bpmn kaydına bak. Turuncu bulut = Proxied, gri = DNS only.
