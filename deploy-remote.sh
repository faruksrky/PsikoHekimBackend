#!/bin/bash
# Lokal makineden sunucuya deploy
# Kullanım: ./deploy-remote.sh [ssh_user@host]
# Örnek:   ./deploy-remote.sh root@187.77.77.215

REMOTE="${1:-root@187.77.77.215}"

echo ">>> Sunucuya bağlanılıyor: $REMOTE"
echo ">>> git pull + ./deploy.sh psikohekim çalıştırılıyor..."
echo ""

ssh "$REMOTE" "cd ~/PsikoHekimBackend && git pull && ./deploy.sh psikohekim"

echo ""
echo ">>> Deploy tamamlandı. https://api.iyihislerapp.com kontrol et."
