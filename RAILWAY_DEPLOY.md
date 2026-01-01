# 🚀 PsikoHekim Backend (BFF) - Railway Deploy Rehberi

## 📋 Proje Bilgileri
- **Proje Adı:** PsikoHekim Backend (BFF)
- **Port:** 8083
- **Tip:** Spring Boot (Java)
- **GitHub Repo:** https://github.com/faruksrky/PsikoHekimBackend.git

---

## 🚀 Deploy Adımları

### 1️⃣ Railway'a Kayıt Olun
- https://railway.app → GitHub ile giriş

### 2️⃣ Yeni Proje Oluşturun
1. **"New Project"** → **"Deploy from GitHub repo"**
2. `PsikoHekimBackend` repo'sunu seçin
3. Railway otomatik olarak Maven projesini algılayacak

### 3️⃣ Environment Variables Ekleyin

Railway → Your Service → **Variables**:

```bash
# Port (Railway otomatik set eder, ama emin olmak için)
PORT=8083

# Database (eğer PostgreSQL kullanıyorsanız)
DATABASE_URL=${DATABASE_URL}
# VEYA Railway'da PostgreSQL ekleyin:
# New → Database → Add PostgreSQL

# Keycloak (Keycloak servisi deploy olduktan sonra)
KEYCLOAK_BASE_URL=https://keycloak-service.up.railway.app
# VEYA
KEYCLOAK_BASE_URL=https://keycloak.iyihislerapp.com

# BPMN (BPMN servisi deploy olduktan sonra)
BPMN_BASE_URL=https://bpmn-service.up.railway.app
# VEYA
BPMN_BASE_URL=https://bpmn.iyihislerapp.com

# Diğer environment variables
# (application.yml veya application.properties'deki tüm değişkenler)
```

### 4️⃣ Port Ayarı

`application.yml` veya `application.properties`'de:
```yaml
server:
  port: ${PORT:8083}
```

### 5️⃣ Deploy URL'ini Alın

Railway → Settings → Networking → **"Generate Domain"**
- URL: `https://psikohekim-backend.up.railway.app`

### 6️⃣ Custom Domain (Opsiyonel)

Railway → Settings → Networking → **Custom Domain**:
- Domain: `bff.iyihislerapp.com`
- DNS kayıtlarını Cloudflare'de yapılandırın

---

## 🔄 Frontend Environment Variables Güncelleme

**Cloudflare Pages → Settings → Environment Variables:**

```bash
VITE_PSIKOHEKIM_BASE_URL=https://psikohekim-backend.up.railway.app
# VEYA custom domain:
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

## ⚠️ Önemli Notlar

1. **Keycloak ve BPMN servisleri önce deploy edilmeli**
2. Deploy sonrası URL'leri environment variables'a ekleyin
3. CORS ayarlarını güncelleyin (Cloudflare Pages URL'lerini allow edin)

