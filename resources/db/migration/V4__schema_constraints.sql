-- ============================================================
-- users
-- ============================================================

-- Keycloak is the identity provider. Every user lookup, update, and delete
-- filters by keycloak_id. Without a UNIQUE constraint the column had no
-- uniqueness enforcement and no index, making those operations sequential
-- scans and allowing duplicate keycloak_id values to be inserted.
ALTER TABLE public.users
    ADD CONSTRAINT users_keycloak_id UNIQUE (keycloak_id);

-- ============================================================
-- firmware_upgrade_rules
-- ============================================================

-- An upgrade path from firmware version A to version B should be defined
-- only once. Duplicate rows cause findFirstByFrom_Id to return ambiguous
-- results and make the upgrade graph inconsistent. Duplicates are removed
-- before the constraint is added; the count is reported so operators can
-- audit unexpected data loss.
DO $$
DECLARE removed_count integer;
BEGIN
    WITH duplicates AS (
        SELECT firmware_upgrade_rule_id,
               ROW_NUMBER() OVER (
                   PARTITION BY from_id, to_id
                   ORDER BY firmware_upgrade_rule_id
               ) AS rn
        FROM public.firmware_upgrade_rules
    )
    DELETE FROM public.firmware_upgrade_rules
    WHERE firmware_upgrade_rule_id IN (
        SELECT firmware_upgrade_rule_id FROM duplicates WHERE rn > 1
    );
    GET DIAGNOSTICS removed_count = ROW_COUNT;
    RAISE NOTICE 'firmware_upgrade_rules: removed % duplicate row(s) before adding UNIQUE constraint', removed_count;
END $$;

ALTER TABLE public.firmware_upgrade_rules
    ADD CONSTRAINT firmware_upgrade_rules_from_to_unique UNIQUE (from_id, to_id);

-- ============================================================
-- rsu_options
-- ============================================================

-- rsu_options is a 1:1 structural extension of the rsus row — each RSU has
-- exactly one options row. An orphaned rsu_options row after the parent RSU
-- is deleted has no operational meaning and blocks re-insertion of an RSU
-- with the same rsu_id. CASCADE removes the child automatically when the
-- parent RSU is deleted.
ALTER TABLE public.rsu_options
    DROP CONSTRAINT IF EXISTS fk_rsu_id,
    ADD CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
        REFERENCES public.rsus (rsu_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE;

-- ============================================================
-- consecutive_firmware_upgrade_failures
-- ============================================================

-- 1:1 structural extension of rsus. Same rationale as rsu_options above:
-- the row has no meaning without its parent RSU, and without CASCADE a
-- pending delete is blocked by the child row.
ALTER TABLE public.consecutive_firmware_upgrade_failures
    DROP CONSTRAINT IF EXISTS fk_rsu_id,
    ADD CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
        REFERENCES public.rsus (rsu_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE;

-- ============================================================
-- snmp_msgfwd_config
-- ============================================================

-- SNMP forwarding config rows are RSU-specific. There is no meaningful
-- forwarding configuration without an owning RSU, and without CASCADE a
-- delete of an RSU that has active forwarding entries is blocked entirely.
ALTER TABLE public.snmp_msgfwd_config
    DROP CONSTRAINT IF EXISTS fk_rsu_id,
    ADD CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
        REFERENCES public.rsus (rsu_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE;

-- ============================================================
-- max_retry_limit_reached_instances
-- ============================================================

-- Retry exhaustion records are tied to a specific RSU. The composite primary
-- key includes rsu_id, so a row cannot be reassigned to another RSU; CASCADE
-- is the only meaningful behaviour on RSU deletion.
ALTER TABLE public.max_retry_limit_reached_instances
    DROP CONSTRAINT IF EXISTS fk_rsu_id,
    ADD CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
        REFERENCES public.rsus (rsu_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE;

-- Note: telemetry tables (ping, rsu_health, scms_health) are intentionally
-- left as RESTRICT. These are high-volume time-series tables; RSU deletion
-- should require explicit data pruning before the parent row can be removed,
-- to prevent accidental bulk data loss.

-- ============================================================
-- rsu_organization
-- ============================================================

-- An RSU can only be assigned to a given organization once. Duplicates are
-- removed before the constraint is added; the count is reported so operators
-- can audit unexpected data loss.
DO $$
DECLARE removed_count integer;
BEGIN
    WITH duplicates AS (
        SELECT rsu_organization_id,
               ROW_NUMBER() OVER (
                   PARTITION BY rsu_id, organization_id
                   ORDER BY rsu_organization_id
               ) AS rn
        FROM public.rsu_organization
    )
    DELETE FROM public.rsu_organization
    WHERE rsu_organization_id IN (
        SELECT rsu_organization_id FROM duplicates WHERE rn > 1
    );
    GET DIAGNOSTICS removed_count = ROW_COUNT;
    RAISE NOTICE 'rsu_organization: removed % duplicate row(s) before adding UNIQUE constraint', removed_count;
END $$;

ALTER TABLE public.rsu_organization
    ADD CONSTRAINT rsu_organization_unique UNIQUE (rsu_id, organization_id);

-- An RSU may belong to multiple organizations (shared-jurisdiction model,
-- e.g. a Region 1 RSU is also visible to CDOT). CASCADE on rsu_id is safe:
-- deleting an RSU legitimately removes all of its organization memberships.
ALTER TABLE public.rsu_organization
    DROP CONSTRAINT IF EXISTS fk_rsu_id,
    ADD CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
        REFERENCES public.rsus (rsu_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE;

-- organization_id is RESTRICT, not CASCADE: the minimum-one-organization rule
-- (every RSU must remain visible to at least one organization) is enforced in
-- the application layer only. admin_org.delete_org_authorized refuses to
-- delete an organization that would orphan an RSU (check_orphan_rsus) and then
-- removes the membership rows explicitly. A CASCADE here would let any other
-- code path bypass that orphan check at the database level.
ALTER TABLE public.rsu_organization
    DROP CONSTRAINT IF EXISTS fk_organization_id,
    ADD CONSTRAINT fk_organization_id FOREIGN KEY (organization_id)
        REFERENCES public.organizations (organization_id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT;

-- ============================================================
-- rsu_intersection
-- ============================================================

-- UNIQUE (rsu_id, intersection_id) is already enforced in the baseline schema.
-- A junction row linking an RSU to an intersection has no meaning once either
-- parent is deleted. CASCADE on both FKs prevents orphaned rows and allows
-- RSU or intersection deletion without requiring manual cleanup first.
ALTER TABLE public.rsu_intersection
    DROP CONSTRAINT IF EXISTS fk_rsu_id,
    ADD CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
        REFERENCES public.rsus (rsu_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE;

ALTER TABLE public.rsu_intersection
    DROP CONSTRAINT IF EXISTS fk_intersection_id,
    ADD CONSTRAINT fk_intersection_id FOREIGN KEY (intersection_id)
        REFERENCES public.intersections (intersection_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE;

-- ============================================================
-- user_organization
-- ============================================================

-- A user can only be assigned to a given organization once (with one role).
DO $$
DECLARE removed_count integer;
BEGIN
    WITH duplicates AS (
        SELECT user_organization_id,
               ROW_NUMBER() OVER (
                   PARTITION BY user_id, organization_id
                   ORDER BY user_organization_id
               ) AS rn
        FROM public.user_organization
    )
    DELETE FROM public.user_organization
    WHERE user_organization_id IN (
        SELECT user_organization_id FROM duplicates WHERE rn > 1
    );
    GET DIAGNOSTICS removed_count = ROW_COUNT;
    RAISE NOTICE 'user_organization: removed % duplicate row(s) before adding UNIQUE constraint', removed_count;
END $$;

ALTER TABLE public.user_organization
    ADD CONSTRAINT user_organization_unique UNIQUE (user_id, organization_id);

-- CASCADE on user_id: the Keycloak custom user provider's removeUser operation
-- deletes directly from public.users without first removing user_organization
-- rows. Without CASCADE the delete fails for any user that has organization
-- memberships. user_email_notification already uses CASCADE on user_id for the
-- same reason; this aligns user_organization with that existing pattern.
ALTER TABLE public.user_organization
    DROP CONSTRAINT IF EXISTS fk_user_id,
    ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id)
        REFERENCES public.users (user_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE;

-- organization_id is RESTRICT, not CASCADE: a user may belong to multiple
-- organizations, and the minimum-one-organization rule is enforced in the
-- application layer only. admin_org.delete_org_authorized refuses to delete
-- an organization that would orphan a user (check_orphan_users) and then
-- removes the membership rows explicitly. A CASCADE here would let any other
-- code path bypass that orphan check at the database level.
--
-- role_id is deliberately left RESTRICT: roles are reference data that are
-- never deleted, and the RESTRICT FK actively prevents a role still assigned
-- to any user from being removed.
ALTER TABLE public.user_organization
    DROP CONSTRAINT IF EXISTS fk_organization_id,
    ADD CONSTRAINT fk_organization_id FOREIGN KEY (organization_id)
        REFERENCES public.organizations (organization_id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT;

-- ============================================================
-- intersection_organization
-- ============================================================

-- An intersection can only be assigned to a given organization once.
DO $$
DECLARE removed_count integer;
BEGIN
    WITH duplicates AS (
        SELECT intersection_organization_id,
               ROW_NUMBER() OVER (
                   PARTITION BY intersection_id, organization_id
                   ORDER BY intersection_organization_id
               ) AS rn
        FROM public.intersection_organization
    )
    DELETE FROM public.intersection_organization
    WHERE intersection_organization_id IN (
        SELECT intersection_organization_id FROM duplicates WHERE rn > 1
    );
    GET DIAGNOSTICS removed_count = ROW_COUNT;
    RAISE NOTICE 'intersection_organization: removed % duplicate row(s) before adding UNIQUE constraint', removed_count;
END $$;

ALTER TABLE public.intersection_organization
    ADD CONSTRAINT intersection_organization_unique UNIQUE (intersection_id, organization_id);

-- An intersection may belong to multiple organizations. CASCADE on
-- intersection_id is safe: deleting an intersection legitimately removes all
-- of its organization memberships.
ALTER TABLE public.intersection_organization
    DROP CONSTRAINT IF EXISTS fk_intersection_id,
    ADD CONSTRAINT fk_intersection_id FOREIGN KEY (intersection_id)
        REFERENCES public.intersections (intersection_id)
        ON UPDATE NO ACTION
        ON DELETE CASCADE;

-- organization_id is RESTRICT, not CASCADE: the minimum-one-organization rule
-- is enforced in the application layer only. admin_org.delete_org_authorized
-- refuses to delete an organization that would orphan an intersection
-- (check_orphan_intersections) and then removes the membership rows
-- explicitly. A CASCADE here would let any other code path bypass that orphan
-- check at the database level.
ALTER TABLE public.intersection_organization
    DROP CONSTRAINT IF EXISTS fk_organization_id,
    ADD CONSTRAINT fk_organization_id FOREIGN KEY (organization_id)
        REFERENCES public.organizations (organization_id)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT;

-- ============================================================
-- rsus.milepost non-negative
-- ============================================================

-- The admin UI rejects negative milepost values (regex /^\d*\.?\d*$/ allows
-- only digits and a decimal point, no leading minus). Without a DB constraint
-- any API client or direct INSERT could store a negative value, which has no
-- physical meaning. Abort the migration if bad data exists so an operator can
-- correct it before the constraint is applied.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM public.rsus WHERE milepost < 0) THEN
        RAISE EXCEPTION 'rsus: one or more rows have milepost < 0 — correct the data before applying this migration';
    END IF;
END $$;

ALTER TABLE public.rsus
    ADD CONSTRAINT rsus_milepost_non_negative CHECK (milepost >= 0);

-- ============================================================
-- users.first_name / last_name NOT NULL
-- ============================================================

-- The admin UI requires both fields on every create/edit form. The baseline
-- schema left them nullable, so direct inserts or legacy data could produce
-- rows the UI would never generate. Backfill NULL to empty string (non-
-- destructive) before tightening the column; the frontend continues to enforce
-- non-empty values on write.
DO $$
DECLARE null_count integer;
BEGIN
    UPDATE public.users SET first_name = '' WHERE first_name IS NULL;
    GET DIAGNOSTICS null_count = ROW_COUNT;
    RAISE NOTICE 'users: backfilled % NULL first_name value(s) to empty string', null_count;

    UPDATE public.users SET last_name = '' WHERE last_name IS NULL;
    GET DIAGNOSTICS null_count = ROW_COUNT;
    RAISE NOTICE 'users: backfilled % NULL last_name value(s) to empty string', null_count;
END $$;

ALTER TABLE public.users
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name  SET NOT NULL;

-- ============================================================
-- roles.name allowed values
-- ============================================================

-- The application recognises exactly three role names: admin, operator, user
-- (lowercase — see R__sample_data.sql and auth-api parseRole). The baseline
-- schema used an open varchar with only a UNIQUE constraint, so a direct
-- INSERT of an unrecognised role name would silently succeed. Abort the
-- migration if any unexpected role name is present.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM public.roles
        WHERE name NOT IN ('admin', 'operator', 'user')
    ) THEN
        RAISE EXCEPTION 'roles: unexpected role name found — only admin, operator, user are allowed';
    END IF;
END $$;

ALTER TABLE public.roles
    ADD CONSTRAINT roles_name_allowed CHECK (name IN ('admin', 'operator', 'user'));

-- ============================================================
-- intersections.intersection_number digits-only
-- ============================================================

-- The admin UI enforces a digits-only regex (/^[0-9]+$/) for intersection
-- numbers, matching the numeric NTCIP intersection IDs used in the field.
-- Abort the migration if any row would violate the constraint.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM public.intersections
        WHERE intersection_number !~ '^[0-9]+$'
    ) THEN
        RAISE EXCEPTION 'intersections: one or more rows have a non-numeric intersection_number — correct the data before applying this migration';
    END IF;
END $$;

ALTER TABLE public.intersections
    ADD CONSTRAINT intersection_number_numeric CHECK (intersection_number ~ '^[0-9]+$');
