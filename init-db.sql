DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'keycloak_admin') THEN
        CREATE USER keycloak_admin WITH PASSWORD 'admin_pass';
    END IF;
END
$$;

ALTER DATABASE keycloak OWNER TO keycloak_admin;

\c keycloak;
GRANT ALL ON SCHEMA public TO keycloak_admin;
ALTER SCHEMA public OWNER TO keycloak_admin;
