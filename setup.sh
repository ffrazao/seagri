#!/bin/bash

# ==========================================================
# PARÂMETROS CONFIGURÁVEIS
# ==========================================================
DB_USER=${DB_ADMIN_USER:-keycloak_admin}
DB_PASS=${DB_ADMIN_PASSWORD:-admin_pass}
PG_DB=${POSTGRES_DB:-keycloak}
PG_ROOT_PASS=${POSTGRES_PASSWORD:-root_pass}
KC_ADMIN=${KC_BOOTSTRAP_ADMIN_USERNAME:-admin}
KC_PASS=${KC_BOOTSTRAP_ADMIN_PASSWORD:-admin}

echo "Limpando ambiente anterior e configurando SEAGRI..."

# 1. Estrutura de pastas
mkdir -p keycloak/logs keycloak/conf postgres/logs postgres/data

# 2. Arquivo .env (Fonte da verdade para o Docker)
cat <<EOF > .env
DB_ADMIN_USER=$DB_USER
DB_ADMIN_PASSWORD=$DB_PASS
POSTGRES_DB=$PG_DB
POSTGRES_PASSWORD=$PG_ROOT_PASS
KC_HOSTNAME=localhost
KC_BOOTSTRAP_ADMIN_USERNAME=$KC_ADMIN
KC_BOOTSTRAP_ADMIN_PASSWORD=$KC_PASS
EOF

# 3. Script SQL Dinâmico (Aqui injetamos as variáveis do Shell no arquivo)
# O use de \$ protege o caractere do Postgres, mas permite que o Shell troque $DB_USER
cat <<EOF > postgres/init-db.sql
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = '$DB_USER') THEN
        CREATE USER $DB_USER WITH PASSWORD '$DB_PASS';
    END IF;
END
\$\$;

ALTER DATABASE $PG_DB OWNER TO $DB_USER;

\c $PG_DB;
GRANT ALL ON SCHEMA public TO $DB_USER;
ALTER SCHEMA public OWNER TO $DB_USER;
EOF

# 4. Docker Compose
cat <<EOF > docker-compose.yml
services:
  postgres:
    image: postgres:16-alpine
    container_name: postgres_db
    environment:
      POSTGRES_DB: \${POSTGRES_DB}
      POSTGRES_PASSWORD: \${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - ./postgres/data:/var/lib/postgresql/data
      - ./postgres/init-db.sql:/docker-entrypoint-initdb.d/init-db.sql:ro
      - ./postgres/logs:/var/log/postgresql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  keycloak:
    image: quay.io/keycloak/keycloak:26.0
    container_name: keycloak_service
    command: start-dev
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/\${POSTGRES_DB}
      KC_DB_USERNAME: \${DB_ADMIN_USER}
      KC_DB_PASSWORD: \${DB_ADMIN_PASSWORD}
      KC_HOSTNAME: localhost
      KC_BOOTSTRAP_ADMIN_USERNAME: \${KC_BOOTSTRAP_ADMIN_USERNAME}
      KC_BOOTSTRAP_ADMIN_PASSWORD: \${KC_BOOTSTRAP_ADMIN_PASSWORD}
    volumes:
      - ./keycloak/conf:/opt/keycloak/conf
      - ./keycloak/logs:/opt/keycloak/logs
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
EOF

# 5. Permissões Linux (Essencial para Ubuntu)
sudo chown -R 999:999 postgres/data postgres/logs
sudo chmod -R 700 postgres/data

# 6. Git Git Init (Estrutura de escalabilidade)
git init
cd postgres && git init && git add . && git commit -m "Módulo DB"
cd ../keycloak && git init && git add . && git commit -m "Módulo KC"
cd ..

echo "------------------------------------------------"
echo "Setup pronto! Rode: docker-compose up -d"