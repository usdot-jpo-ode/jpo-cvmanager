-- R__sample_data.sql
-- Dev seed data for local development and testing only.
-- Do NOT apply to production environments.
-- Flyway re-runs this script whenever its checksum changes.

INSERT INTO public.manufacturers(name)
  VALUES ('Commsignia'), ('Yunex')
  ON CONFLICT (name) DO NOTHING;

INSERT INTO public.rsu_models(name, supported_radio, manufacturer)
  VALUES ('ITS-RS4-M', 'DSRC,C-V2X', 1), ('RSU2X US', 'DSRC,C-V2X', 2)
  ON CONFLICT (name) DO NOTHING;

INSERT INTO public.firmware_images(name, model, install_package, version)
  VALUES ('y20.0.0', 1, 'install_y20_0_0.tar', 'y20.0.0'), ('y20.1.0', 1, 'install_y20_1_0.tar', 'y20.1.0')
  ON CONFLICT (name) DO NOTHING;

INSERT INTO public.firmware_upgrade_rules(from_id, to_id)
  VALUES (1, 2)
  ON CONFLICT DO NOTHING;

INSERT INTO public.organizations(name)
  VALUES ('Test Org'), ('Test Org 2')
  ON CONFLICT (name) DO NOTHING;

INSERT INTO public.rsu_credentials(username, password, nickname, owner_organization_id)
  VALUES ('username', 'password', 'cred1', 1)
  ON CONFLICT (nickname) DO NOTHING;

INSERT INTO public.snmp_credentials(username, password, encrypt_password, nickname, owner_organization_id)
  VALUES ('username', 'password', 'encryption-pw', 'snmp1', 1)
  ON CONFLICT (nickname) DO NOTHING;

INSERT INTO public.snmp_protocols(protocol_code, nickname)
  VALUES ('41', 'RSU 4.1'), ('1218', 'NTCIP 1218')
  ON CONFLICT (nickname) DO NOTHING;

INSERT INTO public.rsus(geography, milepost, ipv4_address, serial_number, iss_scms_id, primary_route, model, credential_id, snmp_credential_id, snmp_protocol_id, firmware_version, target_firmware_version)
  VALUES
    (ST_GeomFromText('POINT(-105.0135030 39.7405654)'), 1, '10.0.0.180', 'E5672', 'E5672', 'I999', 1, 1, 1, 1, 1, 1),
    (ST_GeomFromText('POINT(-104.987775 39.981805)'), 2, '10.0.0.78', 'E5321', 'E5321', 'I999', 1, 1, 1, 2, 2, 2)
  ON CONFLICT DO NOTHING;

INSERT INTO public.rsu_options(rsu_id, tim_deposit, snmp_monitoring)
  VALUES (1, TRUE, TRUE), (2, FALSE, TRUE)
  ON CONFLICT (rsu_id) DO NOTHING;

INSERT INTO public.roles(name)
  VALUES ('admin'), ('operator'), ('user')
  ON CONFLICT (name) DO NOTHING;

INSERT INTO public.rsu_organization(rsu_id, organization_id)
  VALUES (1, 1), (2, 1)
  ON CONFLICT DO NOTHING;

-- Replace email with a real address to test GCP OAuth2.0 support
INSERT INTO public.users(keycloak_id, email, first_name, last_name, created_timestamp, super_user)
  VALUES ('fc3d8729-8526-4aaa-805b-d64bf3b93860'::UUID, 'test@gmail.com', 'Test', 'User', (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000), '1')
  ON CONFLICT (email) DO NOTHING;

INSERT INTO public.user_organization(user_id, organization_id, role_id)
  VALUES (1, 1, 1), (1, 2, 3)
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
    (2, 3, 2, 'SPAT', '10.0.0.80', 44910, '2024/04/01T00:00:00', '2034/04/01T00:00:00', '1', '0')
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

INSERT INTO public.intersections(intersection_number, ref_pt, intersection_name)
  VALUES (12109, ST_GeomFromText('POINT(-105.0908854 39.5880413)'), 'S Wadsworth & W Columbine Dr')
  ON CONFLICT (intersection_number) DO NOTHING;

INSERT INTO public.intersection_organization(intersection_id, organization_id)
  VALUES (1, 1)
  ON CONFLICT DO NOTHING;

INSERT INTO public.rsu_intersection(rsu_id, intersection_id)
  VALUES (1, 1)
  ON CONFLICT DO NOTHING;
