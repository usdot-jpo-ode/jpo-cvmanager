CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Update public.users table definition
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS keycloak_id UUID NOT NULL DEFAULT uuid_generate_v4(),
ADD COLUMN IF NOT EXISTS created_timestamp BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())*1000::BIGINT,
ALTER COLUMN first_name DROP NOT NULL,
ALTER COLUMN last_name DROP NOT NULL,
ALTER COLUMN super_user SET DEFAULT 0::bit;

-- For testing: describe table
-- SELECT column_name, data_type, character_maximum_length, column_default, is_nullable FROM information_schema.columns WHERE table_name = 'users';