#!/usr/bin/env bash
# Creates the nereden role and database for local development.
# Usage:
#   PGPASSWORD=your_postgres_password ./scripts/init-db.sh
# Or with peer auth (no -h):
#   ./scripts/init-db.sh

set -euo pipefail

DB_NAME="${DB_NAME:-nereden}"
DB_USER="${DB_USER:-nereden}"
DB_PASSWORD="${DB_PASSWORD:-nereden}"
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGUSER="${PGUSER:-postgres}"

psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d postgres -v ON_ERROR_STOP=1 <<SQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${DB_USER}') THEN
    CREATE ROLE ${DB_USER} LOGIN PASSWORD '${DB_PASSWORD}';
  ELSE
    ALTER ROLE ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';
  END IF;
END
\$\$;

SELECT 'CREATE DATABASE ${DB_NAME} OWNER ${DB_USER}'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${DB_NAME}')\gexec

GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};
SQL

echo "Database '${DB_NAME}' ready for user '${DB_USER}'."
