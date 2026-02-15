#!/bin/bash
# Sunucuda ilk kurulum - git clone + .env + deploy
# Kullanım: curl -sL https://raw.githubusercontent.com/.../setup-server.sh | bash
# Veya: sunucuya kopyalayıp ./setup-server.sh

set -e

REPO_URL="https://github.com/faruksrky/PsikoHekimBackend.git"
DIR="$HOME/PsikoHekimBackend"

echo ">>> Git clone..."
if [ -d "$DIR" ]; then
  cd "$DIR"
  git pull
else
  git clone "$REPO_URL" "$DIR"
  cd "$DIR"
fi

if [ ! -f .env ]; then
  echo ">>> .env oluşturuluyor (.env.example'dan)..."
  cp .env.example .env
  echo ""
  echo "ÖNEMLİ: .env dosyasını düzenle ve şifreleri doldur:"
  echo "  nano .env"
  echo ""
  echo "Sonra tekrar çalıştır: ./deploy.sh"
  exit 0
fi

echo ">>> Deploy..."
chmod +x deploy.sh
./deploy.sh
