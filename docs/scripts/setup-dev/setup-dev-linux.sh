#!/bin/bash
# Script de configuração inicial do ambiente de desenvolvimento (Linux/Ubuntu)
# Versão agnóstica a arquitetura (amd64 / arm / arm64)

set -e  # Para o script em caso de erro

echo "Iniciando configuração do ambiente de desenvolvimento local (HTTPS)..."

# ====================== 1. Instalação de dependências ======================
echo "[1/4] Verificando dependências do sistema..."
sudo apt update -qq
sudo apt install -y libnss3-tools wget curl

# ====================== 2. Detecção da arquitetura ======================
echo "[2/4] Detectando arquitetura da máquina..."

ARCH=$(uname -m)
case $ARCH in
    x86_64|amd64)
        MKCERT_ARCH="amd64"
        ;;
    aarch64|arm64)
        MKCERT_ARCH="arm64"
        ;;
    armv7l|armv6l|arm)
        MKCERT_ARCH="arm"
        ;;
    *)
        echo "❌ Arquitetura não suportada: $ARCH"
        echo "Arquiteturas suportadas: x86_64, aarch64, armv7l"
        exit 1
        ;;
esac

echo "   → Arquitetura detectada: $ARCH → mkcert-$MKCERT_ARCH"

# ====================== 3. Instalação do mkcert ======================
echo "[3/4] Instalando mkcert..."

# Usa URL estável recomendada pelo autor (melhor que fixar versão)
MKCERT_URL="https://dl.filippo.io/mkcert/latest?for=linux/${MKCERT_ARCH}"

wget -q --show-progress -O /tmp/mkcert "${MKCERT_URL}"
chmod +x /tmp/mkcert
sudo mv /tmp/mkcert /usr/local/bin/mkcert

# Verifica se foi instalado corretamente
if command -v mkcert >/dev/null 2>&1; then
    echo "   → mkcert instalado com sucesso ($(mkcert -version))"
else
    echo "❌ Falha ao instalar mkcert"
    exit 1
fi

# ====================== 4. Geração da CA local ======================
echo "[4/4] Instalando Autoridade Certificadora Local (CA)..."
mkcert -install

# ====================== 5. Geração dos certificados do projeto ======================
echo "   Gerando certificados SSL para o Nginx..."
mkdir -p nginx/certs

# Gera certificados para localhost (pode adicionar mais nomes se quiser)
mkcert -key-file nginx/certs/ip-key.pem -cert-file nginx/certs/ip-cert.pem localhost

echo "✅ Configuração concluída com sucesso!"
echo "   Ambiente pronto para rodar com HTTPS."
echo ""
echo "Execute:"
echo "   docker compose --profile completo up -d --build"