-- R__sample_data.sql
-- Dev seed data for local development and testing only.
-- Do NOT apply to production environments.
-- Flyway re-runs this script whenever its checksum changes.
--
-- Layout:
--   * Every base table seeded here has at least three rows.
--   * RSUs, intersections, and users are seeded so that EACH of the three
--     organizations has at least three of each, using a "hybrid" model: one
--     shared resource is assigned to all three orgs, and each org additionally
--     gets two org-unique resources (shared core + 2 unique = 3 per org).
--   * IDs are assumed to start at 1 on a fresh database (serial sequences emit
--     1, 2, 3 ... in insert order). FK references below rely on that ordering.
--
-- Org membership map (shared core = the first row of each resource):
--   RSUs:          Org1 -> 1,2,3   Org2 -> 1,4,5   Org3 -> 1,6,7
--   Intersections: Org1 -> 1,2,3   Org2 -> 1,4,5   Org3 -> 1,6,7
--   Users:         Org1 -> 1,2,3   Org2 -> 1,4,5   Org3 -> 1,6,7

INSERT INTO public.manufacturers(name)
  VALUES ('Commsignia'), ('Yunex'), ('Kapsch')
  ON CONFLICT (name) DO NOTHING;

INSERT INTO public.rsu_models(name, supported_radio, manufacturer)
  VALUES ('ITS-RS4-M', 'DSRC,C-V2X', 1), ('RSU2X US', 'DSRC,C-V2X', 2), ('RIS-9260', 'C-V2X', 3)
  ON CONFLICT (name) DO NOTHING;

INSERT INTO public.firmware_images(name, model, install_package, version)
  VALUES ('y20.0.0', 1, 'install_y20_0_0.tar', 'y20.0.0'), ('y20.1.0', 1, 'install_y20_1_0.tar', 'y20.1.0'), ('k1.0.0', 3, 'install_k1_0_0.tar', 'k1.0.0')
  ON CONFLICT (name) DO NOTHING;

-- Three upgrade rules across the three firmware images. UNIQUE (from_id, to_id)
-- means a third meaningful rule is only possible because firmware_image 3 exists.
INSERT INTO public.firmware_upgrade_rules(from_id, to_id)
  VALUES (1, 2), (2, 3), (1, 3)
  ON CONFLICT DO NOTHING;

INSERT INTO public.organizations(name)
  VALUES ('Test Org'), ('Test Org 2'), ('Test Org 3')
  ON CONFLICT (name) DO NOTHING;

INSERT INTO public.rsu_credentials(username, password, nickname, owner_organization_id)
  VALUES ('username', 'password', 'cred1', 1),
         ('username2', 'password2', 'cred2', 2),
         ('username3', 'password3', 'cred3', 3)
  ON CONFLICT (nickname) DO NOTHING;

INSERT INTO public.snmp_credentials(username, password, encrypt_password, nickname, owner_organization_id)
  VALUES ('username', 'password', 'encryption-pw', 'snmp1', 1),
         ('username2', 'password2', 'encryption-pw2', 'snmp2', 2),
         ('username3', 'password3', 'encryption-pw3', 'snmp3', 3)
  ON CONFLICT (nickname) DO NOTHING;

INSERT INTO public.snmp_protocols(protocol_code, nickname)
  VALUES ('41', 'RSU 4.1'), ('1218', 'NTCIP 1218'), ('42', 'RSU 4.2')
  ON CONFLICT (nickname) DO NOTHING;

-- Seven RSUs. RSU 1 is the shared core (assigned to all three orgs below); RSUs
-- 2-7 are the org-unique pairs. model-2 RSUs have no matching firmware image, so
-- their firmware columns are left NULL (both columns are nullable).
INSERT INTO public.rsus(geography, milepost, ipv4_address, serial_number, iss_scms_id, primary_route, model, credential_id, snmp_credential_id, snmp_protocol_id, firmware_version, target_firmware_version)
  VALUES
    (ST_GeomFromText('POINT(-105.0135030 39.7405654)'), 1, '10.0.0.11', 'E0001', 'I0001', 'I25',  1, 1, 1, 1, 1,    2),
    (ST_GeomFromText('POINT(-104.9877750 39.9818050)'), 2, '10.0.0.12', 'E0002', 'I0002', 'I25',  1, 1, 1, 1, 1,    2),
    (ST_GeomFromText('POINT(-105.0908854 39.5880413)'), 3, '10.0.0.13', 'E0003', 'I0003', 'I25',  2, 2, 2, 2, NULL, NULL),
    (ST_GeomFromText('POINT(-104.9712000 39.7392000)'), 4, '10.0.0.14', 'E0004', 'I0004', 'I70',  2, 2, 2, 2, NULL, NULL),
    (ST_GeomFromText('POINT(-104.8214000 39.7280000)'), 5, '10.0.0.15', 'E0005', 'I0005', 'I70',  3, 3, 3, 3, 3,    3),
    (ST_GeomFromText('POINT(-105.2705000 40.0150000)'), 6, '10.0.0.16', 'E0006', 'I0006', 'I70',  3, 3, 3, 3, 3,    3),
    (ST_GeomFromText('POINT(-104.8319000 38.8339000)'), 7, '10.0.0.17', 'E0007', 'I0007', 'US36', 1, 1, 1, 1, 1,    2)
  ON CONFLICT DO NOTHING;

-- One rsu_options row per RSU (1:1 extension). A few have tim_deposit = true to
-- populate the partial index idx_rsu_options_tim_deposit.
INSERT INTO public.rsu_options(rsu_id, tim_deposit, snmp_monitoring)
  VALUES (1, TRUE, TRUE), (2, FALSE, TRUE), (3, TRUE, TRUE), (4, FALSE, FALSE),
         (5, TRUE, FALSE), (6, FALSE, TRUE), (7, TRUE, TRUE)
  ON CONFLICT (rsu_id) DO NOTHING;

INSERT INTO public.roles(name)
  VALUES ('admin'), ('operator'), ('user')
  ON CONFLICT (name) DO NOTHING;

-- RSU 1 is shared across all three orgs; each org gets two more unique RSUs, so
-- every org has exactly three RSUs.
INSERT INTO public.rsu_organization(rsu_id, organization_id)
  VALUES (1, 1), (2, 1), (3, 1),
         (1, 2), (4, 2), (5, 2),
         (1, 3), (6, 3), (7, 3)
  ON CONFLICT DO NOTHING;

-- Replace user 1's email with a real address to test GCP OAuth2.0 support.
-- User 1 is the shared core (member of all three orgs); users 2-7 are org-unique.
INSERT INTO public.users(keycloak_id, email, first_name, last_name, created_timestamp, super_user)
  VALUES
    ('fc3d8729-8526-4aaa-805b-d64bf3b93860'::UUID, 'test@gmail.com',  'Test',  'User', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000), '1'),
    ('a1b2c3d4-0000-4aaa-805b-000000000002'::UUID, 'test2@gmail.com', 'Test2', 'User', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000), '0'),
    ('a1b2c3d4-0000-4aaa-805b-000000000003'::UUID, 'test3@gmail.com', 'Test3', 'User', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000), '0'),
    ('a1b2c3d4-0000-4aaa-805b-000000000004'::UUID, 'test4@gmail.com', 'Test4', 'User', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000), '0'),
    ('a1b2c3d4-0000-4aaa-805b-000000000005'::UUID, 'test5@gmail.com', 'Test5', 'User', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000), '0'),
    ('a1b2c3d4-0000-4aaa-805b-000000000006'::UUID, 'test6@gmail.com', 'Test6', 'User', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000), '0'),
    ('a1b2c3d4-0000-4aaa-805b-000000000007'::UUID, 'test7@gmail.com', 'Test7', 'User', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000), '0')
  ON CONFLICT (email) DO NOTHING;

-- User 1 is shared across all three orgs; each org gets two more unique users, so
-- every org has exactly three members. Roles cycle admin/operator/user.
INSERT INTO public.user_organization(user_id, organization_id, role_id)
  VALUES (1, 1, 1), (2, 1, 2), (3, 1, 3),
         (1, 2, 1), (4, 2, 2), (5, 2, 3),
         (1, 3, 1), (6, 3, 2), (7, 3, 3)
  ON CONFLICT DO NOTHING;

INSERT INTO public.snmp_msgfwd_type(name)
  VALUES ('rsuDsrcFwd'), ('rsuReceivedMsg'), ('rsuXmitMsgFwding')
  ON CONFLICT (name) DO NOTHING;

INSERT INTO public.snmp_msgfwd_config(rsu_id, msgfwd_type, snmp_index, message_type, dest_ipv4, dest_port, start_datetime, end_datetime, active, security)
  VALUES
    (1, 1, 1, 'BSM', '10.0.0.80', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1', '0'),
    (1, 1, 2, 'BSM', '10.0.0.81', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1', '0'),
    (1, 1, 3, 'BSM', '10.0.0.82', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1', '1'),
    (2, 2, 1, 'BSM', '10.0.0.80', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1', '1'),
    (2, 2, 2, 'BSM', '10.0.0.81', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1', '1'),
    (2, 3, 1, 'MAP', '10.0.0.80', 44920, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1', '1'),
    (2, 3, 2, 'SPAT', '10.0.0.80', 44910, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1', '0'),
    (3, 1, 1, 'BSM', '10.0.0.80', 46800, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1', '0')
  ON CONFLICT DO NOTHING;

INSERT INTO public.email_type(email_type, required_role, description, supports_immediate, supports_hourly, supports_daily, supports_weekly, supports_monthly)
  VALUES
    ('Support Requests',                 1, 'Receive support requests from users', true,  false, false, false, false),
    ('Firmware Upgrade Failures',        2, 'Receive automated firmware upgrade failure emails', true,  false, false, false, false),
    ('Daily Message Counts',             3, 'Receive automated daily message count emails', false, false, true, false, false),
    ('Access Requests',                  1, 'Receive organization access requests from users', true,  false, false, false, false),
    ('Intersection Notification Summary',3, 'Receive automated intersection notification summary emails', true,  true,  true,  true,  true),
    ('Critical Error Messages',          2, 'Receive automated critical error message emails', true,  false, false, false, false)
  ON CONFLICT (email_type) DO UPDATE SET
    required_role      = EXCLUDED.required_role,
    description        = EXCLUDED.description,
    supports_immediate = EXCLUDED.supports_immediate,
    supports_hourly    = EXCLUDED.supports_hourly,
    supports_daily     = EXCLUDED.supports_daily,
    supports_weekly    = EXCLUDED.supports_weekly,
    supports_monthly   = EXCLUDED.supports_monthly;

INSERT INTO public.user_email_notification(user_email_notification_id, user_id, email_type_id, immediate, hourly, daily, weekly, monthly)
  VALUES
    (1, 1, 1, true, false, false, false, false),
    (2, 1, 2, true, false, false, false, false),
    (3, 1, 3, false, false, true, false, false),
    (4, 1, 4, true, false, false, false, false),
    (5, 1, 5, true, true, true, true, true),
    (6, 1, 6, true, false, false, false, false)
  ON CONFLICT DO NOTHING;

-- Seven intersections. Intersection 1 is the shared core (assigned to all three
-- orgs below); intersections 2-7 are the org-unique pairs.
INSERT INTO public.intersections(intersection_number, ref_pt, intersection_name)
  VALUES
    (12109, ST_GeomFromText('POINT(-105.0908854 39.5880413)'), 'S Wadsworth & W Columbine Dr'),
    (12110, ST_GeomFromText('POINT(-104.9876000 39.7392000)'), 'E Colfax Ave & N Broadway'),
    (12111, ST_GeomFromText('POINT(-104.8910000 39.7000000)'), 'S Parker Rd & E Hampden Ave'),
    (12112, ST_GeomFromText('POINT(-105.0178000 39.7625000)'), 'Federal Blvd & W 38th Ave'),
    (12113, ST_GeomFromText('POINT(-104.9390000 39.6766000)'), 'S Colorado Blvd & E Evans Ave'),
    (12114, ST_GeomFromText('POINT(-105.2110000 39.7150000)'), 'US-6 & 6th Ave Pkwy'),
    (12115, ST_GeomFromText('POINT(-104.7560000 39.8560000)'), 'E-470 & Pena Blvd')
  ON CONFLICT (intersection_number) DO NOTHING;

-- Intersection 1 is shared across all three orgs; each org gets two more unique
-- intersections, so every org has exactly three.
INSERT INTO public.intersection_organization(intersection_id, organization_id)
  VALUES (1, 1), (2, 1), (3, 1),
         (1, 2), (4, 2), (5, 2),
         (1, 3), (6, 3), (7, 3)
  ON CONFLICT DO NOTHING;

-- A few RSU <-> intersection links (UNIQUE rsu_id, intersection_id).
INSERT INTO public.rsu_intersection(rsu_id, intersection_id)
  VALUES (1, 1), (2, 2), (3, 3)
  ON CONFLICT DO NOTHING;

-- Firmware-failure child tables (CASCADE on RSU delete). Three rows each, on
-- distinct RSUs. consecutive_firmware_upgrade_failures has a single-column rsu_id
-- PK; max_retry_limit_reached_instances has a (rsu_id, reached_at) PK and its
-- target_firmware_version references firmware_images.
INSERT INTO public.consecutive_firmware_upgrade_failures(rsu_id, consecutive_failures)
  VALUES (5, 2), (6, 1), (7, 3)
  ON CONFLICT (rsu_id) DO NOTHING;

INSERT INTO public.max_retry_limit_reached_instances(rsu_id, reached_at, target_firmware_version)
  VALUES
    (5, '2026/01/01T00:00:00', 3),
    (6, '2026/01/01T00:00:00', 3),
    (7, '2026/01/01T00:00:00', 2)
  ON CONFLICT DO NOTHING;

-- Telemetry tables have serial PKs with no natural unique key, so each block is
-- guarded by NOT EXISTS to stay idempotent across checksum re-runs. Seeded for
-- RSU 1 and RSU 2.
INSERT INTO public.ping(timestamp, result, rsu_id)
  SELECT ts, res, rid
  FROM (VALUES
    ('2026/06/01T00:00:00'::timestamp, B'1', 1),
    ('2026/06/01T00:05:00'::timestamp, B'1', 1),
    ('2026/06/01T00:10:00'::timestamp, B'0', 1),
    ('2026/06/01T00:00:00'::timestamp, B'1', 2),
    ('2026/06/01T00:05:00'::timestamp, B'0', 2),
    ('2026/06/01T00:10:00'::timestamp, B'1', 2)
  ) AS seed(ts, res, rid)
  WHERE NOT EXISTS (SELECT 1 FROM public.ping);

INSERT INTO public.rsu_health(timestamp, health, rsu_id)
  SELECT ts, hlth, rid
  FROM (VALUES
    ('2026/06/01T00:00:00'::timestamp, 1, 1),
    ('2026/06/01T00:05:00'::timestamp, 1, 1),
    ('2026/06/01T00:10:00'::timestamp, 0, 1),
    ('2026/06/01T00:00:00'::timestamp, 1, 2),
    ('2026/06/01T00:05:00'::timestamp, 0, 2),
    ('2026/06/01T00:10:00'::timestamp, 1, 2)
  ) AS seed(ts, hlth, rid)
  WHERE NOT EXISTS (SELECT 1 FROM public.rsu_health);

INSERT INTO public.scms_health(timestamp, health, expiration, rsu_id)
  SELECT ts, hlth, exp, rid
  FROM (VALUES
    ('2026/06/01T00:00:00'::timestamp, B'1', '2027/06/01T00:00:00'::timestamp, 1),
    ('2026/06/01T00:05:00'::timestamp, B'1', '2027/06/01T00:00:00'::timestamp, 1),
    ('2026/06/01T00:10:00'::timestamp, B'0', '2027/06/01T00:00:00'::timestamp, 1),
    ('2026/06/01T00:00:00'::timestamp, B'1', '2027/06/01T00:00:00'::timestamp, 2),
    ('2026/06/01T00:05:00'::timestamp, B'1', '2027/06/01T00:00:00'::timestamp, 2),
    ('2026/06/01T00:10:00'::timestamp, B'0', '2027/06/01T00:00:00'::timestamp, 2)
  ) AS seed(ts, hlth, exp, rid)
  WHERE NOT EXISTS (SELECT 1 FROM public.scms_health);
