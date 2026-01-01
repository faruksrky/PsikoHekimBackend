# 🚀 PsikoHekim Backend (BFF) - Railway Deploy Rehberi

## 📋 Proje Bilgileri
- **Proje Adı:** PsikoHekim Backend (BFF)
- **Port:** 8083
- **Tip:** Spring Boot (Java 21)
- **Build:** Maven
- **Dizin:** `/Users/fs648/Desktop/PsikoHekim/PsikoHekimBackend`

---

## 🚀 Deploy Adımları

### 1️⃣ GitHub Repo Hazırlığı

```bash
cd /Users/fs648/Desktop/PsikoHekim/PsikoHekimBackend

# Git repo kontrol (submodule olabilir)
git remote -v

# Eğer submodule ise, ayrı repo oluşturun:
# GitHub'da: https://github.com/new
# Repo adı: psikohekim-backend

git remote set-url origin https://github.com/YOUR_USERNAME/psikohekim-backend.git

# Commit ve push
git add .
git commit -m "Railway deploy ready"
git push -u origin main
```

### 2️⃣ Railway'a Deploy

1. Railway Dashboard → **"New Project"** (veya mevcut projeye **"New"** → **"GitHub Repo"**)
2. `psikohekim-backend` repo'sunu seçin

### 3️⃣ Database ve Redis Ekleyin

Railway'da:
1. **"New"** → **"Database"** → **"Add PostgreSQL"**
2. **"New"** → **"Database"** → **"Add Redis"** (veya Redis için başka bir servis)

### 4️⃣ Environment Variables

Railway → Variables:

```bash
# Port
PORT=8083

# Database (Railway PostgreSQL otomatik set eder)
DATABASE_URL=${DATABASE_URL}
# VEYA manuel:
# SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
# SPRING_DATASOURCE_USERNAME=user
# SPRING_DATASOURCE_PASSWORD=pass

# Redis
REDIS_HOST=${REDIS_HOST}
REDIS_PORT=${REDIS_PORT}
REDIS_PASSWORD=${REDIS_PASSWORD:}

# Keycloak Service URL (Keycloak deploy olduktan sonra)
SERVICES_KEYCLOAK_URL=https://keycloak-auth-service.up.railway.app
# VEYA
SERVICES_KEYCLOAK_URL=https://keycloak.iyihislerapp.com

# BPMN Service URL (BPMN deploy olduktan sonra)
SERVICES_BPMN_URL=https://bpmn-service.up.railway.app
# VEYA
SERVICES_BPMN_URL=https://bpmn.iyihislerapp.com

# Zeebe
ZEBBE_CLIENT_BROKER_GATEWAY_ADDRESS=localhost:26500
# VEYA
ZEBBE_CLIENT_BROKER_GATEWAY_ADDRESS=zeebe.railway.app:26500

# Webhook
WEBHOOK_BASE_URL=https://psikohekim-backend.up.railway.app
WEBHOOK_PATH=/webhooks/whatsapp

# Twilio (secrets klasöründen)
TWILIO_ACCOUNT_SID=${TWILIO_ACCOUNT_SID}
TWILIO_AUTH_TOKEN=${TWILIO_AUTH_TOKEN}
TWILIO_WHATSAPP_FROM=${TWILIO_WHATSAPP_FROM}

# Microsoft Graph (secrets klasöründen)
MICROSOFT_GRAPH_CLIENT_ID=${MICROSOFT_GRAPH_CLIENT_ID}
MICROSOFT_GRAPH_CLIENT_SECRET=${MICROSOFT_GRAPH_CLIENT_SECRET}
MICROSOFT_GRAPH_TENANT_ID=${MICROSOFT_GRAPH_TENANT_ID}
MICROSOFT_GRAPH_REDIRECT_URI=${MICROSOFT_GRAPH_REDIRECT_URI}

# Google OAuth (secrets klasöründen)
GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
GOOGLE_REDIRECT_URI=${GOOGLE_REDIRECT_URI}
```

### 5️⃣ Port ve Database Ayarı

`application-dev.yml` veya `application-prod.yml`:

```yaml
server:
  port: ${PORT:8083}

spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5433/psikohekim}
    # Railway DATABASE_URL otomatik olarak kullanılacak
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

services:
  bpmn:
    url: ${SERVICES_BPMN_URL:http://localhost:8082}

webhook:
  base-url: ${WEBHOOK_BASE_URL:http://localhost:8083}
```

### 6️⃣ Secrets Klasörü

**NOT:** `secrets/` klasöründeki dosyalar Railway environment variables olarak eklenmeli.

Secrets klasöründeki her dosya için Railway'a environment variable ekleyin:
- `twilio.account.sid` → `TWILIO_ACCOUNT_SID`
- `twilio.auth.token` → `TWILIO_AUTH_TOKEN`
- vb.

### 7️⃣ Deploy URL

Railway → Settings → Networking → **"Generate Domain"**
- URL: `https://psikohekim-backend.up.railway.app`

### 8️⃣ Custom Domain

- Domain: `bff.iyihislerapp.com`

---

## 🔄 Deployment Sonrası

### Cloudflare Pages Environment Variables

```bash
VITE_PSIKOHEKIM_BASE_URL=https://psikohekim-backend.up.railway.app
# VEYA
VITE_PSIKOHEKIM_BASE_URL=https://bff.iyihislerapp.com

# Endpoint'ler
VITE_PATIENT_LIST_URL=${VITE_PSIKOHEKIM_BASE_URL}/patient/all
VITE_PATIENT_DETAILS_URL=${VITE_PSIKOHEKIM_BASE_URL}/patient/details
VITE_PATIENT_SEARCH_URL=${VITE_PSIKOHEKIM_BASE_URL}/patient/search
VITE_PATIENT_ADD_URL=/patient/addPatient

VITE_THERAPIST_LIST_URL=${VITE_PSIKOHEKIM_BASE_URL}/therapist/all
VITE_THERAPIST_DETAILS_URL=${VITE_PSIKOHEKIM_BASE_URL}/therapist/details
VITE_THERAPIST_SEARCH_URL=${VITE_PSIKOHEKIM_BASE_URL}/therapist/search
VITE_THERAPIST_ADD_URL=${VITE_PSIKOHEKIM_BASE_URL}/therapist/addTherapist

VITE_THERAPIST_PATIENT_PATIENTS_URL=${VITE_PSIKOHEKIM_BASE_URL}/therapist-patient
```

---

## ✅ Checklist

- [ ] GitHub repo hazır
- [ ] Railway'da proje oluşturuldu
- [ ] PostgreSQL database eklendi
- [ ] Redis eklendi
- [ ] Environment variables eklendi (secrets dahil)
- [ ] Port ve database ayarları güncellendi
- [ ] Deploy URL alındı
- [ ] Cloudflare Pages environment variables güncellendi
- [ ] Test edildi ✅

