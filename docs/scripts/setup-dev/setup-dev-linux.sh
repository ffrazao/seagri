#!/bin/bash
# Script de configuração inicial do ambiente de desenvolvimento (Linux/Ubuntu)
# Versão agnóstica + IP da máquina

set -e

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

# ====================== 5. Detecção de IP da máquina ======================
echo "   Detectando IP principal da máquina..."

# Métodos robustos para pegar o IP (prioridade: interface padrão)
PRIMARY_IP=$(ip route get 1 2>/dev/null | awk '{print $7}' | head -n1)
if [ -z "$PRIMARY_IP" ]; then
    PRIMARY_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
fi

if [ -n "$PRIMARY_IP" ]; then
    echo "   → IP detectado: $PRIMARY_IP"
    EXTRA_NAMES="$PRIMARY_IP"
else
    echo "   ⚠️  Não foi possível detectar o IP automaticamente"
    EXTRA_NAMES=""
fi

# ====================== 6. Geração dos certificados ======================
echo "   Gerando certificados SSL para o Nginx..."

CERT_DIR="nginx/certs"
if [ -d "$CERT_DIR" ]; then
    echo "   → Ajustando permissões do diretório $CERT_DIR..."
    sudo chown -R "$USER:$USER" "$CERT_DIR" 2>/dev/null || true
else
    mkdir -p "$CERT_DIR"
fi

# Remove arquivos antigos para evitar conflito
rm -f "$CERT_DIR/ip-key.pem" "$CERT_DIR/ip-cert.pem" 2>/dev/null || true

# Gera o certificado com localhost + IP
if [ -n "$EXTRA_NAMES" ]; then
    mkcert -key-file "$CERT_DIR/ip-key.pem" \
           -cert-file "$CERT_DIR/ip-cert.pem" \
           localhost $EXTRA_NAMES
else
    mkcert -key-file "$CERT_DIR/ip-key.pem" \
           -cert-file "$CERT_DIR/ip-cert.pem" \
           localhost
fi

echo ""
echo "✅ Configuração concluída com sucesso!"
echo "   Certificados gerados em: $CERT_DIR"
echo "   Nomes incluídos: localhost${EXTRA_NAMES:+, $EXTRA_NAMES}"
echo ""
echo "Execute:"
echo "   docker compose --profile completo up -d --build"

