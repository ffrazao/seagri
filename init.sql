-- Script de inicialização do PostgreSQL
-- Criado em: $(date)

-- Criar usuários primeiro (isso pode ficar dentro do DO)
DO $$
BEGIN
    -- API
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'api_user') THEN
        CREATE USER api_user WITH PASSWORD 'api_password_789';
        RAISE NOTICE 'Usuário api_user criado';
    ELSE
        RAISE NOTICE 'Usuário api_user já existe';
    END IF;
    
    -- APP1
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'usuario_app1') THEN
        CREATE USER usuario_app1 WITH PASSWORD 'senha_segura_123';
        RAISE NOTICE 'Usuário usuario_app1 criado';
    ELSE
        RAISE NOTICE 'Usuário usuario_app1 já existe';
    END IF;
    
    -- APP2
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'user_app') THEN
        CREATE USER user_app WITH PASSWORD 'outra_senha_456';
        RAISE NOTICE 'Usuário user_app criado';
    ELSE
        RAISE NOTICE 'Usuário user_app já existe';
    END IF;
END $$;

-- Criar bancos de dados (fora do DO, porque CREATE DATABASE não pode ser executado em transação)
SELECT 'CREATE DATABASE api_db' 
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'api_db')\gexec

SELECT 'CREATE DATABASE meu_app' 
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'meu_app')\gexec

SELECT 'CREATE DATABASE aplicacao' 
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'aplicacao')\gexec

-- Conceder privilégios nos bancos
GRANT ALL PRIVILEGES ON DATABASE api_db TO api_user;
GRANT ALL PRIVILEGES ON DATABASE meu_app TO usuario_app1;
GRANT ALL PRIVILEGES ON DATABASE aplicacao TO user_app;

-- Conectar a cada banco e conceder privilégios no schema public
\c api_db
GRANT ALL ON SCHEMA public TO api_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO api_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO api_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO api_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO api_user;

\c meu_app
GRANT ALL ON SCHEMA public TO usuario_app1;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO usuario_app1;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO usuario_app1;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO usuario_app1;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO usuario_app1;

\c aplicacao
GRANT ALL ON SCHEMA public TO user_app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO user_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO user_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO user_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO user_app;

\c postgres
-- Mensagem final
DO $$
BEGIN
    RAISE NOTICE '=========================================';
    RAISE NOTICE 'Inicialização concluída com sucesso!';
    RAISE NOTICE 'Bancos criados: api_db, meu_app, aplicacao';
    RAISE NOTICE 'Usuários criados: api_user, usuario_app1, user_app';
    RAISE NOTICE '=========================================';
END $$;
