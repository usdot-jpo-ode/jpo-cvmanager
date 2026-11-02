-- V13__convert_bit_columns_to_boolean.sql
-- Converts every true/false column stored as bit(1) to the native boolean type.
--
--   users.super_user DEFAULT 0::bit -> boolean DEFAULT false
--   ping.result
--   scms_health.health
--   snmp_msgfwd_config.active
--   snmp_msgfwd_config.security
--   obu_ota_requests.error_status
--
-- Mapping: B'1' -> true, B'0' -> false, NULL -> NULL.
--
-- Release note: every component that reads or writes these columns (Keycloak
-- custom user provider, Intersection API, rsu-info-bridge, Python API/common
-- and addons, webapp) must be deployed together with this migration.
--
-- Each ALTER rewrites its table. ping and scms_health are telemetry tables
-- and should already be kept small (see table comments), but the rewrite
-- holds an ACCESS EXCLUSIVE lock for its duration.

BEGIN;

ALTER TABLE public.users
    ALTER COLUMN super_user DROP DEFAULT,
    ALTER COLUMN super_user TYPE boolean USING (super_user = B'1'),
    ALTER COLUMN super_user SET DEFAULT false;

ALTER TABLE public.ping
    ALTER COLUMN result TYPE boolean USING (result = B'1');

ALTER TABLE public.scms_health
    ALTER COLUMN health TYPE boolean USING (health = B'1');

ALTER TABLE public.snmp_msgfwd_config
    ALTER COLUMN active TYPE boolean USING (active = B'1'),
    ALTER COLUMN security TYPE boolean USING (security = B'1');

ALTER TABLE public.obu_ota_requests
    ALTER COLUMN error_status TYPE boolean USING (error_status = B'1');

-- Column comments that described the old 0/1 encoding.
COMMENT ON TABLE public.users IS
    'Users authorized to access the CV Manager. Keycloak is the identity provider; keycloak_id links to the Keycloak user record. Users with super_user=true can access the admin panel across all organizations.';

COMMENT ON COLUMN public.users.super_user IS
    'true = user can access the admin panel and manage resources across all organizations. false = normal user.';

-- Surface any remaining bit(1) columns. They are not converted automatically
-- because they may not represent true/false values.
DO $$
DECLARE
    remaining text;
BEGIN
    SELECT string_agg(format('%I.%I.%I', table_schema, table_name, column_name), ', ')
    INTO remaining
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND data_type = 'bit'
      AND character_maximum_length = 1;
    IF remaining IS NOT NULL THEN
        RAISE WARNING 'bit(1) columns still present after V13: %', remaining;
    END IF;
END $$;

COMMIT;
