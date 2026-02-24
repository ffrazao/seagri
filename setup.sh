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

# --- NOVIDADE: Credenciais do banco isolado da Aplicação ---
BACKEND_DB_USER=${BACKEND_DB_USER:-backend_user}
BACKEND_DB_PASS=${BACKEND_DB_PASSWORD:-backend_pass}
BACKEND_DB_NAME=${BACKEND_DB_NAME:-backend_db}

echo "Limpando e reconstruindo ambiente SEAGRI..."

# 1. Criar estrutura de diretórios no Host
mkdir -p keycloak/logs keycloak/conf
mkdir -p postgres/logs postgres/data postgres/conf

# --- NOVIDADE: Criar estrutura de diretórios do Backend (Monolito Modular) ---
PACKAGE_BASE="backend/src/main/java/br/gov/df/seagri"
mkdir -p "$PACKAGE_BASE/core_domain"
mkdir -p "$PACKAGE_BASE/organization_module"
mkdir -p "$PACKAGE_BASE/attendance_module"
mkdir -p "$PACKAGE_BASE/biometric_module"
mkdir -p "$PACKAGE_BASE/audit_module"
mkdir -p "$PACKAGE_BASE/infrastructure"
mkdir -p "backend/src/main/resources/db/migration"

# 1.1 Criar arquivo de configuração do Keycloak
cat << EOF > keycloak/conf/keycloak.conf
# Configurações de Banco de Dados
db=postgres
db-url=jdbc:postgresql://postgres:5432/$PG_DB

# Configurações de Log
log=file
log-file=/opt/keycloak/logs/keycloak.log

# Configurações de HTTP/Rede
http-enabled=true
hostname-strict=false
EOF

# 2. Criar o arquivo .env
cat << EOF > .env
DB_ADMIN_USER=$DB_USER
DB_ADMIN_PASSWORD=$DB_PASS
POSTGRES_DB=$PG_DB
POSTGRES_PASSWORD=$PG_ROOT_PASS
KC_HOSTNAME=localhost
KC_BOOTSTRAP_ADMIN_USERNAME=$KC_ADMIN
KC_BOOTSTRAP_ADMIN_PASSWORD=$KC_PASS
BACKEND_DB_NAME=$BACKEND_DB_NAME
BACKEND_DB_USER=$BACKEND_DB_USER
BACKEND_DB_PASSWORD=$BACKEND_DB_PASS
EOF

# 3. Criar script de inicialização do Postgres (Alterado para criar 2 bancos)
cat << EOF > postgres/init-db.sh
#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "\$POSTGRES_USER" --dbname "\$POSTGRES_DB" <<-EOSQL
    DO \$\$
    BEGIN
        -- Garante que o usuário do Keycloak exista
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = '$DB_USER') THEN
            CREATE USER $DB_USER WITH PASSWORD '$DB_PASS';
        END IF;

        -- Garante que o usuário da SEAGRI exista
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = '$BACKEND_DB_USER') THEN
            CREATE USER $BACKEND_DB_USER WITH PASSWORD '$BACKEND_DB_PASS';
        END IF;
    END
    \$\$;

    -- Configurações e permissões do banco do Keycloak
    ALTER DATABASE $PG_DB OWNER TO $DB_USER;
    \c $PG_DB;
    GRANT ALL ON SCHEMA public TO $DB_USER;
    ALTER SCHEMA public OWNER TO $DB_USER;

    -- Criação do banco da SEAGRI 
    \c "\$POSTGRES_DB";
    SELECT 'CREATE DATABASE $BACKEND_DB_NAME OWNER $BACKEND_DB_USER'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$BACKEND_DB_NAME')\gexec

    -- Configurações e permissões do banco da SEAGRI
    \c $BACKEND_DB_NAME;
    GRANT ALL ON SCHEMA public TO $BACKEND_DB_USER;
    ALTER SCHEMA public OWNER TO $BACKEND_DB_USER;
EOSQL
EOF

chmod +x postgres/init-db.sh

# 4. Criar docker-compose.yml (Alterado com o esqueleto do backend)
cat << EOF > docker-compose.yml
services:
  postgres:
    image: postgres:16-alpine
    container_name: postgres_db
    environment:
      POSTGRES_DB: \${POSTGRES_DB}
      POSTGRES_PASSWORD: \${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    command: >
      postgres
      -c logging_collector=on
      -c log_directory=/var/log/postgresql
      -c log_filename=postgresql.log
      -c log_file_mode=0666
    volumes:
      - ./postgres/data:/var/lib/postgresql/data
      - ./postgres/init-db.sh:/docker-entrypoint-initdb.d/init-db.sh:ro
      - ./postgres/logs:/var/log/postgresql
      - ./postgres/conf:/etc/postgresql-custom
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
      KC_LOG: file
      KC_LOG_FILE: /opt/keycloak/logs/keycloak.log
    volumes:
      - ./keycloak/conf:/opt/keycloak/conf
      - ./keycloak/logs:/opt/keycloak/logs
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy

  # --- ESQUELETO DO BACKEND SPRING BOOT (Descomentar futuramente) ---
  # backend:
  #   image: openjdk:21-jdk-slim
  #   container_name: seagri_backend
  #   ports:
  #     - "8081:8080"
  #   environment:
  #     SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/$BACKEND_DB_NAME
  #     SPRING_DATASOURCE_USERNAME: $BACKEND_DB_USER
  #     SPRING_DATASOURCE_PASSWORD: $BACKEND_DB_PASS
  #   depends_on:
  #     postgres:
  #       condition: service_healthy

EOF

# 5. Ajuste de Permissões
echo "Ajustando permissões para os usuários internos do Docker..."
sudo chmod -R 777 postgres/data postgres/logs postgres/conf
sudo chmod -R 777 keycloak/logs keycloak/conf

# 6. Configurar Git
if [ ! -d ".git" ]; then git init; fi

cat << EOF > .gitignore
.env
*/data/
*/logs/
backend/target/
*.jar
EOF

echo "------------------------------------------------"
echo "Setup concluído! Agora execute: docker-compose up -d"
echo "------------------------------------------------"
echo ""
echo "------------------------------------------------"
echo "Aguarde 20 segundos até o sistema subir completamente e execute:"
echo "------------------------------------------------"
echo "# Extrair configuração do Postgres"
echo "docker cp postgres_db:/var/lib/postgresql/data/postgresql.conf ./postgres/conf/"
echo "docker cp postgres_db:/var/lib/postgresql/data/pg_hba.conf ./postgres/conf/"
echo ""
echo "# Extrair configuração do Keycloak"
echo "docker cp keycloak_service:/opt/keycloak/conf/keycloak.conf ./keycloak/conf/"
