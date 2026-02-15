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
