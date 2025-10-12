-- EventPulse Database Initialization Script
-- This script runs when the PostgreSQL container is first created

-- Create database if it doesn't exist (already created by POSTGRES_DB env var)
-- But we can add additional setup here

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET timezone = 'UTC';

-- Create any additional schemas if needed
-- CREATE SCHEMA IF NOT EXISTS eventpulse;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE eventpulse TO eventpulse;

-- Create any additional users or roles if needed
-- CREATE ROLE eventpulse_readonly;
-- GRANT CONNECT ON DATABASE eventpulse TO eventpulse_readonly;
-- GRANT USAGE ON SCHEMA public TO eventpulse_readonly;
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO eventpulse_readonly;

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'EventPulse database initialized successfully';
END $$;

