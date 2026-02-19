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

echo "Limpando e reconstruindo ambiente SEAGRI com logs e confs visíveis..."

# 1. Criar estrutura de diretórios
mkdir -p keycloak/logs keycloak/conf
mkdir -p postgres/logs postgres/data postgres/conf

# 2. Criar o arquivo .env
cat <<EOF > .env
DB_ADMIN_USER=$DB_USER
DB_ADMIN_PASSWORD=$DB_PASS
POSTGRES_DB=$PG_DB
POSTGRES_PASSWORD=$PG_ROOT_PASS
KC_HOSTNAME=localhost
KC_BOOTSTRAP_ADMIN_USERNAME=$KC_ADMIN
KC_BOOTSTRAP_ADMIN_PASSWORD=$KC_PASS
EOF

# 3. Criar script de inicialização do Postgres (Dinâmico)
cat <<EOF > postgres/init-db.sh
#!/bin/bash
set -e
psql -v ON_ERROR_STOP=1 --username "\$POSTGRES_USER" --dbname "\$POSTGRES_DB" <<-EOSQL
    DO \\\$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = '$DB_USER') THEN
            CREATE USER $DB_USER WITH PASSWORD '$DB_PASS';
        END IF;
    END
    \\\$\$;
    ALTER DATABASE $PG_DB OWNER TO $DB_USER;
    \c $PG_DB;
    GRANT ALL ON SCHEMA public TO $DB_USER;
    ALTER SCHEMA public OWNER TO $DB_USER;
EOSQL
EOF
chmod +x postgres/init-db.sh

# 4. Criar docker-compose.yml
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
    command: >
      postgres
      -c logging_collector=on
      -c log_directory=/var/log/postgresql
      -c log_filename=postgresql.log
    volumes:
      - ./postgres/data:/var/lib/postgresql/data
      - ./postgres/init-db.sh:/docker-entrypoint-initdb.d/init-db.sh:ro
      - ./postgres/logs:/var/log/postgresql
      - ./postgres/conf:/etc/postgresql
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
EOF

# 5. Ajuste de Permissões (Ubuntu/Linux)
# 999: Postgres, 1000: Keycloak
echo "Ajustando permissões de escrita para os containers..."
sudo chown -R 999:999 postgres/data postgres/logs postgres/conf
sudo chown -R 1000:1000 keycloak/logs keycloak/conf
sudo chmod -R 775 postgres/logs keycloak/logs

# 6. Configurar Git e .gitignore
cat <<EOF > .gitignore
.env
*/data/
*/logs/
EOF

echo "------------------------------------------------"
echo "Setup concluído!"
echo "IMPORTANTE: Para ver os arquivos de config agora pela primeira vez, execute:"
echo "1. docker-compose up -d"
echo "2. docker cp postgres_db:/var/lib/postgresql/data/postgresql.conf ./postgres/conf/"
echo "3. docker cp keycloak_service:/opt/keycloak/conf/keycloak.conf ./keycloak/conf/"
