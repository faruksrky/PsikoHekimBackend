# PsikoHekim Backend - VPS Deploy (Git ile)

## İlk Kurulum

Sunucuda (SSH ile bağlandıktan sonra):

```bash
# 1. Repoyu klonla
cd ~
git clone https://github.com/faruksrky/PsikoHekimBackend.git
cd PsikoHekimBackend

# 2. .env oluştur (şifreleri doldur)
cp .env.example .env
nano .env

# 3. Deploy
chmod +x deploy.sh
./deploy.sh
```

## Güncelleme (her deploy'da)

```bash
cd ~/PsikoHekimBackend
git pull
./deploy.sh
```

`.env` değişmediyse sadece `git pull` + `./deploy.sh` yeterli.

## HTTPS (Nginx + Let's Encrypt)

**Önce:** Cloudflare/DNS'te `auth.iyihislerapp.com` ve `api.iyihislerapp.com` → sunucu IP (187.77.77.215)

```bash
cd ~/PsikoHekimBackend/nginx
sudo bash setup-ssl.sh
# veya email ile: sudo bash setup-ssl.sh senin@email.com
```

Sonra `.env` güncelle:
```
KEYCLOAK_ISSUER_URI=https://auth.iyihislerapp.com/realms/psikohekim
KEYCLOAK_HOSTNAME=auth.iyihislerapp.com
WEBHOOK_BASE_URL=https://api.iyihislerapp.com
GOOGLE_REDIRECT_URI=https://api.iyihislerapp.com/api/google-calendar/callback
```

`./deploy.sh` ile backend'i yeniden başlat.

---

## Şifre Kuralları

**Tüm şifreler için:** En az 12 karakter, büyük harf, küçük harf, rakam ve özel karakter (!@#$%^&* vb.) kullan.

**Keycloak realm şifre politikası (kullanıcı şifreleri):**
1. Keycloak Admin Console → http://SUNUCU:8080
2. Realm seç (psikohekim) → **Authentication** → **Policies** sekmesi
3. **Password Policy** → Add policy:
   - **Minimum length**: 12
   - **Digits**: 1
   - **Uppercase characters**: 1
   - **Lowercase characters**: 1
   - **Special characters**: 1
4. **Save**
