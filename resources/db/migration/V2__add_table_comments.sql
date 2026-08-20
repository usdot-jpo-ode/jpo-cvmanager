-- V202605211729__add_table_comments.sql
-- Adds COMMENT ON TABLE / COLUMN for all tables and the rsu_organization_name view.
-- Preserves documentation previously maintained only in resources/deprecated/sql_scripts/README.md.
--
-- Each statement is wrapped in a DO block that silently skips if the object does not exist.
-- This tolerates databases that were baselined at V1 before all update scripts were applied.

BEGIN;

-- Firmware / hardware catalog
DO $$ BEGIN COMMENT ON TABLE public.manufacturers IS
    'RSU and OBU manufacturers supported by this deployment. Tested manufacturers: Commsignia, Kapsch, Yunex.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.rsu_models IS
    'RSU hardware models. Each model is linked to a manufacturer and identifies firmware upgrade availability.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.firmware_images IS
    'Known RSU firmware packages. Stores the information the API needs to retrieve and install firmware on an RSU.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.firmware_upgrade_rules IS
    'Valid firmware upgrade paths. A from_id->to_id row means an RSU on from_id firmware can upgrade directly to to_id. Without a matching row the upgrade is blocked to prevent skipping intermediate versions.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

-- Credentials
DO $$ BEGIN COMMENT ON TABLE public.rsu_credentials IS
    'SSH credentials for RSU remote access (reboots, firmware upgrades). Referenced by nickname so credentials are never transmitted over the network.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.snmp_credentials IS
    'SNMP credentials for RSU message forwarding configuration. Referenced by nickname so credentials are never transmitted over the network.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.snmp_protocols IS
    'SNMP protocol versions used by RSUs for message forwarding. Referenced by nickname.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

-- Core RSU data
DO $$ BEGIN COMMENT ON TABLE public.rsus IS
    'All RSUs managed by this deployment. Each row appears on the CV Manager map. primary_route is a denormalized field stored here rather than in a separate table.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON COLUMN public.rsus.primary_route IS
    'Denormalized route name (e.g., I-25, US-36). No separate route table exists.';
EXCEPTION WHEN undefined_table OR undefined_column THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.rsu_options IS
    'Per-RSU feature flags. tim_deposit enables TIM message depositing; snmp_monitoring enables SNMP-based health monitoring for this RSU.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

-- Health / status
DO $$ BEGIN COMMENT ON TABLE public.ping IS
    'RSU online/offline ping results. Keep at most 24 hours of records per RSU (or only the most recent record per RSU). Allowing this table to grow large degrades CV Manager map load times. Populated by Zabbix or an automated ping script.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.rsu_health IS
    'RSU health status records collected via SNMP monitoring. Similar to ping — keep recent data only to avoid performance impact on the map.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.scms_health IS
    'ISS SCMS certificate health status per RSU. Populated by polling the ISS SCMS API every 6 hours. Requires an active ISS SCMS service agreement.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.iss_keys IS
    'ISS SCMS API authentication tokens used by the iss_health_check service to query certificate status on behalf of RSUs.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

-- Users and organizations
DO $$ BEGIN COMMENT ON TABLE public.roles IS
    'User roles assignable within an organization. Three rows are required by the application: admin, operator, and user. Do not rename or remove these rows.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON COLUMN public.roles.name IS
    'Required values: ''admin'', ''operator'', ''user''. The application depends on these exact role names.';
EXCEPTION WHEN undefined_table OR undefined_column THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.users IS
    'Users authorized to access the CV Manager. Keycloak is the identity provider; keycloak_id links to the Keycloak user record. Users with super_user=1 can access the admin panel across all organizations.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON COLUMN public.users.super_user IS
    '1 = user can access the admin panel and manage resources across all organizations. 0 = normal user.';
EXCEPTION WHEN undefined_table OR undefined_column THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.organizations IS
    'Deployment organizations. Users and RSUs are scoped to organizations; a user can only access RSUs within their own organizations.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.user_organization IS
    'Many-to-many assignment of users to organizations, with a role per membership.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.rsu_organization IS
    'Many-to-many assignment of RSUs to organizations.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

-- SNMP message forwarding
DO $$ BEGIN COMMENT ON TABLE public.snmp_msgfwd_type IS
    'Lookup table for SNMP message forwarding types (e.g., RX, TX).';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.snmp_msgfwd_config IS
    'Active SNMP message forwarding rules per RSU. Defines which message types to forward, to what destination IP/port, and over what time window.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

-- Email notifications
DO $$ BEGIN COMMENT ON TABLE public.email_type IS
    'Lookup table for notification email categories available for user subscription.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.user_email_notification IS
    'User subscriptions to email notification types, including per-subscription frequency settings.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

-- OBU
DO $$ BEGIN COMMENT ON TABLE public.obu_ota_requests IS
    'Over-the-air firmware update requests for OBU (On-Board Unit) devices.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

-- Intersections
DO $$ BEGIN COMMENT ON TABLE public.intersections IS
    'Managed signalized intersections used by the intersection management features.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.intersection_organization IS
    'Many-to-many assignment of intersections to organizations.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.rsu_intersection IS
    'Association between RSUs and nearby intersections.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

-- Firmware retry tracking
DO $$ BEGIN COMMENT ON TABLE public.consecutive_firmware_upgrade_failures IS
    'Tracks consecutive firmware upgrade failure counts per RSU, used to enforce retry limits before escalating.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

DO $$ BEGIN COMMENT ON TABLE public.max_retry_limit_reached_instances IS
    'Records instances where an RSU has reached the configured maximum consecutive firmware upgrade failure limit.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

-- View
DO $$ BEGIN COMMENT ON VIEW public.rsu_organization_name IS
    'Convenience view joining rsu_organization with organization names for display in the CV Manager UI.';
EXCEPTION WHEN undefined_table THEN NULL; END $$;

COMMIT;
