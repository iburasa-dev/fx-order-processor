-- ============================================================================
-- FX Order Processor - Application User Provisioning Script
-- Creates dedicated non-superuser role with least privilege required for API
-- ============================================================================

-- Step 1: Create application user role with strong password
DO
$do$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'app_user') THEN
      CREATE USER app_user WITH ENCRYPTED PASSWORD 'AppUserSecure2026!';
   ELSE
      ALTER USER app_user WITH ENCRYPTED PASSWORD 'AppUserSecure2026!';
   END IF;
END
$do$;

-- Step 2: Grant database connection
GRANT CONNECT ON DATABASE fx_orders_db TO app_user;

-- Step 3: Grant schema usage
GRANT USAGE, CREATE ON SCHEMA public TO app_user;

-- Step 4: Grant DML operations on all existing tables
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;

-- Step 5: Grant sequence operations for BIGSERIAL auto-increment columns
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- Step 6: Configure default privileges for any future tables and sequences
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO app_user;
