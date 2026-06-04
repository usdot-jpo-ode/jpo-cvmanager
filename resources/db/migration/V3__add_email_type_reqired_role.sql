-- V3__add_email_type_required_role.sql
-- Adds a required_role column to public.email_type, linking each notification
-- email category to the minimum role a user must hold (within their organization)
-- to receive that email type.
--
-- Steps performed:
--   1. Add required_role (integer, nullable) with a FK to public.roles(role_id).
--   2. Backfill all existing rows with a sensible default (role_id 1 = ADMIN)
--      so the subsequent NOT NULL constraint can be applied safely.
--   3. Assign the correct role per email_type:
--        - ADMIN   (1): Support Requests, Access Requests
--        - OPERATOR (2): Firmware Upgrade Failures, Critical Error Messages
--        - USER    (3): Daily Message Counts, Intersection Notification Summary
--   4. Set the column NOT NULL now that every row has a value.
--
-- The column is intentionally added as nullable first (step 1) to allow a smooth
-- transition on databases that already contain email_type rows, avoiding a
-- constraint violation before the backfill (step 2-3) has run.

BEGIN;

-- Update public.email_type table definition
-- omit NOT NULL constraint on required_role for now to allow for smooth transition, will set to NOT NULL after backfilling data
ALTER TABLE public.email_type
ADD COLUMN IF NOT EXISTS required_role integer;

-- Add foreign key constraint
ALTER TABLE public.email_type
ADD CONSTRAINT fk_role_id FOREIGN KEY (required_role)
   REFERENCES public.roles (role_id) MATCH SIMPLE
   ON UPDATE NO ACTION
   ON DELETE NO ACTION;

-- Set default value for all existing entries to role_id 1 (ADMIN)
UPDATE public.email_type
SET required_role = 1
WHERE required_role IS NULL;

-- ADMIN roles
UPDATE public.email_type
SET required_role = 1
WHERE email_type IN ('Support Requests', 'Access Requests');

-- OPERATOR roles
UPDATE public.email_type
SET required_role = 2
WHERE email_type IN ('Firmware Upgrade Failures', 'Critical Error Messages');

-- USER roles
UPDATE public.email_type
SET required_role = 3
WHERE email_type IN ('Daily Message Counts', 'Intersection Notification Summary');

-- Make the column NOT NULL after setting all values
ALTER TABLE public.email_type
ALTER COLUMN required_role SET NOT NULL;

COMMIT;