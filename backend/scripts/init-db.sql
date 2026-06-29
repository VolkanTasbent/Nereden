-- Run as a PostgreSQL superuser (e.g. psql -d postgres -f scripts/init-db.sql)

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'nereden') THEN
        CREATE ROLE nereden LOGIN PASSWORD 'nereden';
    END IF;
END
$$;

SELECT 'CREATE DATABASE nereden OWNER nereden'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'nereden')\gexec

GRANT ALL PRIVILEGES ON DATABASE nereden TO nereden;

\c nereden

GRANT ALL ON SCHEMA public TO nereden;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO nereden;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO nereden;
