# Keycloak Realm Import

Bu klasör `psikohekim` realm'ini Keycloak başlangıcında otomatik import eder.

## İçerik

- **psikohekim-realm.json**: Realm, client, roller ve admin kullanıcı
  - Realm: `psikohekim`
  - Client: `psikohekim-frontend` (redirect: iyihislerapp.com, localhost)
  - Roller: `admin`, `user`
  - Admin kullanıcı: `psikohekimofis@gmail.com`

## İlk giriş

Admin kullanıcı ilk girişte **şifre değiştirmek zorundadır**.

- **Geçici şifre:** `ChangeMe123!`
- İlk girişten sonra Keycloak yeni şifre isteyecek

## Kullanım

```bash
cd PsikoHekimBackend
docker compose -f docker-compose/docker-compose.keycloak-fresh.yml --env-file .env up -d
```

Realm ilk başlatmada otomatik import edilir. Realm zaten varsa import atlanır.
