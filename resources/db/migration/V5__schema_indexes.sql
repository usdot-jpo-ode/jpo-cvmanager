-- ============================================================
-- Drop redundant and unused indexes
-- ============================================================

-- PostgreSQL automatically creates an index to enforce each PRIMARY KEY and
-- UNIQUE constraint. Explicit indexes on those same columns duplicate the
-- implicit index, consume extra storage, and add write overhead on every
-- INSERT/UPDATE/DELETE for zero read benefit. The query planner uses the
-- constraint-backed index with identical plans.

-- Redundant with users_pkey (PRIMARY KEY constraint)
DROP INDEX IF EXISTS public.idx_users_user_id;

-- Redundant with users_email UNIQUE constraint
DROP INDEX IF EXISTS public.idx_users_email;

-- Redundant with intersection_pkey (PRIMARY KEY constraint)
DROP INDEX IF EXISTS public.idx_intersection_id;

-- Redundant with intersection_intersection_number UNIQUE constraint
DROP INDEX IF EXISTS public.idx_intersections_intersection_number;

-- Redundant with rsu_ipv4_address UNIQUE constraint
DROP INDEX IF EXISTS public.idx_rsus_ipv4_address;

-- Redundant with organizations_name UNIQUE constraint
DROP INDEX IF EXISTS public.idx_organizations_name;

-- Redundant: ipv4_address uniquely identifies one row, so rsu_id is always
-- determined by the first column lookup. The UNIQUE constraint index on
-- ipv4_address alone serves all known query patterns against rsus.
DROP INDEX IF EXISTS public.idx_rsus_ipv4_rsu_id;

-- Made redundant by the user_organization_unique UNIQUE constraint added in V4.
-- Both cover (user_id, organization_id) in the same order.
DROP INDEX IF EXISTS public.idx_user_organization;

-- Unused: no query filters scms_health by timestamp without also filtering by
-- rsu_id. Replaced below by the composite (rsu_id, timestamp DESC) index.
DROP INDEX IF EXISTS public.idx_scms_health_timestamp;

-- ============================================================
-- ping
-- ============================================================

-- rsu_id is a FK column — PostgreSQL does not create indexes on FK columns
-- automatically. Without this index every per-RSU ping query (WHERE rsu_id = ?)
-- is a full sequential scan of the entire ping table. The trailing timestamp DESC
-- column covers ORDER BY timestamp DESC result sets and pruning queries that
-- filter by rsu_id before deleting old rows.
CREATE INDEX idx_ping_rsu_id_timestamp
    ON public.ping (rsu_id, timestamp DESC);

COMMENT ON INDEX public.idx_ping_rsu_id_timestamp IS
    'Covers per-RSU ping lookups (WHERE rsu_id = ?) and timestamp-ordered results. '
    'rsu_id is a FK column — PostgreSQL does not index FK columns automatically. '
    'Without this index every per-RSU map load query is a full sequential scan. '
    'Also accelerates pruning queries that filter by rsu_id before deleting old rows.';

-- ============================================================
-- rsu_health
-- ============================================================

-- Same rationale as idx_ping_rsu_id_timestamp above. rsu_health mirrors the
-- ping table in growth pattern and query shape.
CREATE INDEX idx_rsu_health_rsu_id_timestamp
    ON public.rsu_health (rsu_id, timestamp DESC);

COMMENT ON INDEX public.idx_rsu_health_rsu_id_timestamp IS
    'Same rationale as idx_ping_rsu_id_timestamp. '
    'rsu_health mirrors the ping table in growth pattern and query shape.';

-- ============================================================
-- scms_health
-- ============================================================

-- Replaces the dropped idx_scms_health_timestamp (single-column, unused).
-- The leading rsu_id column supports per-RSU certificate health lookups.
-- The trailing timestamp DESC column allows PostgreSQL to satisfy
-- ROW_NUMBER() OVER (PARTITION BY rsu_id ORDER BY timestamp DESC) by walking
-- the index in partition order rather than sorting in memory.
CREATE INDEX idx_scms_health_rsu_id_timestamp
    ON public.scms_health (rsu_id, timestamp DESC);

COMMENT ON INDEX public.idx_scms_health_rsu_id_timestamp IS
    'Replaces the dropped idx_scms_health_timestamp (single-column, unused). '
    'The leading rsu_id column supports per-RSU certificate health lookups. '
    'The trailing timestamp DESC column allows PostgreSQL to satisfy '
    'ROW_NUMBER() OVER (PARTITION BY rsu_id ORDER BY timestamp DESC) '
    'by walking the index in partition order rather than sorting in memory.';

-- ============================================================
-- user_organization
-- ============================================================

-- The UNIQUE constraint (user_id, organization_id) added in V4 is user-first
-- and supports lookups that start from the user side. UserOrganizationRepository
-- also has queries that start from the organization side (findByOrganization_Name,
-- findByUserAndOrganization_Name). Without this index those queries require a full
-- sequential scan of user_organization filtered via a join to organizations.
CREATE INDEX idx_user_organization_organization_id
    ON public.user_organization (organization_id);

COMMENT ON INDEX public.idx_user_organization_organization_id IS
    'Supports org-first lookups in UserOrganizationRepository: findByOrganization_Name '
    'and findByUserAndOrganization_Name. The UNIQUE constraint (user_id, organization_id) '
    'is user-first and does not cover these query patterns.';

-- ============================================================
-- user_email_notification
-- ============================================================

-- All three bulk notification recipient queries in UserEmailNotificationRepository
-- filter by email_type_id as the leading predicate. Without this index each query
-- is a full sequential scan of the entire notification table.
CREATE INDEX idx_user_email_notification_email_type_id
    ON public.user_email_notification (email_type_id);

COMMENT ON INDEX public.idx_user_email_notification_email_type_id IS
    'Supports all three bulk notification recipient queries in UserEmailNotificationRepository, '
    'each of which filters by email_type_id as the leading predicate. FK column; '
    'PostgreSQL does not create indexes on FK columns automatically.';

-- ============================================================
-- firmware_upgrade_rules
-- ============================================================

-- FirmwareUpgradeRuleRepository.findFirstByFrom_Id resolves the allowed upgrade
-- path for a given firmware version. Without this index every upgrade path
-- resolution is a full sequential scan of firmware_upgrade_rules.
CREATE INDEX idx_firmware_upgrade_rules_from_id
    ON public.firmware_upgrade_rules (from_id);

COMMENT ON INDEX public.idx_firmware_upgrade_rules_from_id IS
    'Supports FirmwareUpgradeRuleRepository.findFirstByFrom_Id, which resolves the '
    'allowed upgrade path for a given firmware version. FK column without index '
    'causes a full sequential scan on every upgrade path resolution.';

-- ============================================================
-- rsu_options
-- ============================================================

-- Partial index covering only rows where tim_deposit = true.
-- RsuRepository.findByRsuOptionTimDepositIsTrue (rsu-info-bridge) enumerates all
-- TIM-deposit-enabled RSUs. The partial index is deliberately small: only RSUs
-- with the flag set are indexed, which matches the query predicate exactly.
CREATE INDEX idx_rsu_options_tim_deposit
    ON public.rsu_options (rsu_id)
    WHERE tim_deposit = true;

COMMENT ON INDEX public.idx_rsu_options_tim_deposit IS
    'Partial index covering only rows where tim_deposit = true. '
    'Supports rsu-info-bridge RsuRepository.findByRsuOptionTimDepositIsTrue, which '
    'enumerates all TIM-deposit-enabled RSUs. The partial index is deliberately '
    'small: only RSUs with the flag set are indexed.';

-- ============================================================
-- rsus
-- ============================================================

-- RsuRepository executes SELECT DISTINCT primary_route FROM rsus ORDER BY
-- primary_route ASC to populate the primary route dropdown. Without this index
-- the query requires a full table scan with an in-memory sort. With the index
-- PostgreSQL walks it in order and returns distinct values directly.
CREATE INDEX idx_rsus_primary_route
    ON public.rsus (primary_route);

COMMENT ON INDEX public.idx_rsus_primary_route IS
    'Supports SELECT DISTINCT primary_route FROM rsus ORDER BY primary_route ASC '
    'used by RsuRepository to populate the primary route dropdown. Without this '
    'index the query is a full table scan with an in-memory sort. With the index '
    'PostgreSQL can walk it in order and return distinct values directly.';

-- ============================================================
-- rsu_intersection
-- ============================================================

-- The UNIQUE constraint (rsu_id, intersection_id) is rsu-first and supports
-- RSU-first lookups. RsuIntersectionRepository also has queries and DELETE
-- operations that start from the intersection side (by intersection_number,
-- which resolves to intersection_id). Without this index those operations scan
-- all rows in rsu_intersection for every intersection-based lookup or deletion.
CREATE INDEX idx_rsu_intersection_intersection_id
    ON public.rsu_intersection (intersection_id);

COMMENT ON INDEX public.idx_rsu_intersection_intersection_id IS
    'Supports intersection-first queries and DELETE operations in '
    'RsuIntersectionRepository (e.g. deleteByIntersection_IntersectionNumber, '
    'DELETE WHERE intersection.intersectionNumber = ? AND rsu.ipv4Address IN (...)). '
    'The UNIQUE constraint (rsu_id, intersection_id) is rsu-first and does not '
    'cover these patterns. FK column; PostgreSQL does not index FK columns automatically.';
