#!/bin/bash
# Script de configuração inicial do ambiente de desenvolvimento (Linux/Ubuntu)
# Suporte a IP interno + IP público via argumento

set -e

echo "Iniciando configuração do ambiente de desenvolvimento local (HTTPS)..."

# ====================== 1. Dependências ======================
echo "[1/4] Verificando dependências do sistema..."
sudo apt update -qq
sudo apt install -y libnss3-tools wget curl

# ====================== 2. Arquitetura ======================
echo "[2/4] Detectando arquitetura..."

ARCH=$(uname -m)
case $ARCH in
    x86_64|amd64)  MKCERT_ARCH="amd64" ;;
    aarch64|arm64) MKCERT_ARCH="arm64" ;;
    armv7l|arm)    MKCERT_ARCH="arm" ;;
    *) echo "❌ Arquitetura não suportada: $ARCH"; exit 1 ;;
esac

echo "   → Arquitetura detectada: $ARCH"

# ====================== 3. mkcert ======================
echo "[3/4] Instalando mkcert..."
MKCERT_URL="https://dl.filippo.io/mkcert/latest?for=linux/${MKCERT_ARCH}"
wget -q --show-progress -O /tmp/mkcert "${MKCERT_URL}"
chmod +x /tmp/mkcert
sudo mv /tmp/mkcert /usr/local/bin/mkcert

echo "   → mkcert instalado ($(mkcert -version))"

# ====================== 4. CA Local ======================
echo "[4/4] Instalando Autoridade Certificadora Local..."
mkcert -install

# ====================== 5. Detecção de IPs ======================
echo "   Detectando IPs..."

INTERNAL_IP=$(ip route get 1 2>/dev/null | awk '{print $7}' | head -n1)
if [ -z "$INTERNAL_IP" ]; then
    INTERNAL_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
fi

PUBLIC_IP="" 
if command -v curl >/dev/null; then
    PUBLIC_IP=$(curl -s --max-time 3 ifconfig.me || curl -s --max-time 3 icanhazip.com || true)
fi

echo "   → IP Interno : ${INTERNAL_IP:-não detectado}"
[ -n "$PUBLIC_IP" ] && echo "   → IP Público : $PUBLIC_IP"

# ====================== 6. Nomes para o certificado ======================
NAMES="localhost $INTERNAL_IP"

# Permite passar IP público ou outros nomes como argumento
if [ -n "$1" ]; then
    echo "   → Adicionando nome extra: $1"
    NAMES="$NAMES $1"
elif [ -n "$PUBLIC_IP" ]; then
    echo "   → (Dica: rode com './setup-dev-linux.sh SEU_IP_PUBLICO' para incluir IP externo)"
fi

# ====================== 7. Geração do certificado ======================
echo "   Gerando certificados SSL..."

CERT_DIR="nginx/certs"
mkdir -p "$CERT_DIR"
sudo chown -R "$USER:$USER" "$CERT_DIR" 2>/dev/null || true
rm -f "$CERT_DIR/ip-key.pem" "$CERT_DIR/ip-cert.pem" 2>/dev/null || true

mkcert -key-file "$CERT_DIR/ip-key.pem" \
       -cert-file "$CERT_DIR/ip-cert.pem" \
       $NAMES

echo ""
echo "✅ Configuração concluída com sucesso!"
echo "   Nomes no certificado: $NAMES"
echo "   Certificados em: $CERT_DIR"
echo ""
echo "Execute:"
echo "   docker compose --profile completo up -d --build"