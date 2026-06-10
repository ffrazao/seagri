#!/bin/bash
# Script de configuração inicial do ambiente de desenvolvimento (Linux/Ubuntu)
# Versão agnóstica a arquitetura (amd64 / arm64 / arm)

set -e  # Para o script em caso de erro

echo "Iniciando configuração do ambiente de desenvolvimento local (HTTPS)..."

# ====================== 1. Dependências ======================
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
        exit 1
        ;;
esac

echo "   → Arquitetura detectada: $ARCH → mkcert-$MKCERT_ARCH"

# ====================== 3. Instalação do mkcert ======================
echo "[3/4] Instalando mkcert..."

MKCERT_URL="https://dl.filippo.io/mkcert/latest?for=linux/${MKCERT_ARCH}"

wget -q --show-progress -O /tmp/mkcert "${MKCERT_URL}"
chmod +x /tmp/mkcert
sudo mv /tmp/mkcert /usr/local/bin/mkcert

if command -v mkcert >/dev/null 2>&1; then
    echo "   → mkcert instalado com sucesso ($(mkcert -version))"
else
    echo "❌ Falha ao instalar mkcert"
    exit 1
fi

# ====================== 4. CA Local ======================
echo "[4/4] Instalando Autoridade Certificadora Local (CA)..."
mkcert -install

# ====================== 5. Certificados do projeto ======================
echo "   Gerando certificados SSL para o Nginx..."

# Garante que o diretório existe e tem permissão do usuário atual
CERT_DIR="nginx/certs"
if [ -d "$CERT_DIR" ]; then
    echo "   → Diretório $CERT_DIR já existe. Ajustando permissões..."
    sudo chown -R "$USER:$USER" "$CERT_DIR" 2>/dev/null || true
else
    mkdir -p "$CERT_DIR"
fi

# Remove arquivos antigos com permissão errada (se existirem)
rm -f "$CERT_DIR/ip-key.pem" "$CERT_DIR/ip-cert.pem" 2>/dev/null || true

mkcert -key-file "$CERT_DIR/ip-key.pem" \
       -cert-file "$CERT_DIR/ip-cert.pem" \
       localhost

echo ""
echo "✅ Configuração concluída com sucesso!"
echo "   Certificados gerados em: $CERT_DIR"
echo ""
echo "Execute:"
echo "   docker compose --profile completo up -d --build"