#!/bin/bash
# Script de configuração inicial do ambiente de desenvolvimento (Linux/Ubuntu)

echo "Iniciando configuração do ambiente de desenvolvimento local (HTTPS)..."

# 1. Instalação de dependências
echo "[1/4] Verificando dependências do sistema..."
sudo apt update
sudo apt install -y libnss3-tools wget

# 2. Instalação do mkcert
echo "[2/4] Instalando mkcert..."
wget -O mkcert https://github.com/FiloSottile/mkcert/releases/download/v1.4.4/mkcert-v1.4.4-linux-amd64
chmod +x mkcert
sudo mv mkcert /usr/local/bin/

# 3. Geração da CA local
echo "[3/4] Instalando Autoridade Certificadora Local (CA)..."
mkcert -install

# 4. Geração dos certificados do projeto
echo "[4/4] Gerando certificados SSL para o Nginx..."
mkdir -p nginx/certs
mkcert -key-file nginx/certs/localhost-key.pem -cert-file nginx/certs/localhost.pem localhost

echo "✅ Configuração concluída! O ambiente está pronto para rodar com HTTPS."
echo "Execute: docker compose --profile completo up -d --build"

