# Sunucu Log Kontrol Komutları

## Randevu İstekleri / Inbox Boş mu? (BPMN + Zeebe)

Akış: Randevu isteği → BPMN start-process → Zeebe süreci → send-assignment-request webhook → DB'ye TherapistAssignment → Inbox listesi

**Önemli:** Frontend, danışmanın oluşturduğu randevularda `variables.requiresAdminApproval: true` gönderir. Zeebe işçisi (worker) `/process/send-assignment-request` çağrısına bu alanı **JSON gövdesine** eklemelidir; backend `adminApprovalOnly=true` kaydeder ve kayıt **yalnızca admin** gelen kutusunda (`/process/inbox/pending` parametresiz) görünür, danışmanın kendi inbox filtresinde çıkar.

**1. BPMN servisi çalışıyor mu?**
```bash
docker ps | grep -i bpmn
curl -s -o /dev/null -w "%{http_code}" https://bpmn.iyihislerapp.com/api/bpmn/tasks?processInstanceId=test
# 200 veya 404 beklenir, 502/000 = BPMN down
```

**2. Backend BPMN'ye ulaşabiliyor mu?**
```bash
docker logs psikohekim-backend 2>&1 | grep -i BPMN | tail -50
# "BPMN Feign hatası" veya "Connection refused" varsa BPMN erişilemiyor
```

**3. start-process isteği başarılı mı?**
```bash
docker logs psikohekim-backend 2>&1 | grep "start-process" | tail -20
# "BPMN start-process proxy: messageName=..." görülmeli
```

**4. DB'de PENDING atama var mı?**
```bash
docker exec psikohekim-postgres psql -U postgres -d psikohekim -c "SELECT assignment_id, patient_id, therapist_id, status, process_instance_key, created_at FROM therapist_assignment WHERE status='PENDING' ORDER BY created_at DESC LIMIT 10;"
```

---

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
