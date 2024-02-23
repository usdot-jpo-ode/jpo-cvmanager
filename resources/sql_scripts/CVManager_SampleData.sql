-- Sample data to test CV Manager locally or in a new deployment
-- This is only recommended to be used for testing, do not use in a deployed environment
INSERT INTO public.manufacturers(name) 
  VALUES ('Commsignia'), ('Yunex');

INSERT INTO public.rsu_models(
	name, supported_radio, manufacturer)
	VALUES ('ITS-RS4-M', 'DSRC,C-V2X', 1), ('RSU2X US', 'DSRC,C-V2X', 2);

INSERT INTO public.firmware_images(
	name, model, install_package, version)
	VALUES ('y20.0.0', 1, 'install_y20_0_0.tar', 'y20.0.0'), ('y20.1.0', 1, 'install_y20_1_0.tar', 'y20.1.0');

INSERT INTO public.firmware_upgrade_rules(
	from_id, to_id)
	VALUES (1, 2);

INSERT INTO public.rsu_credentials(
	username, password, nickname)
	VALUES ('username', 'password', 'cred1');

INSERT INTO public.snmp_credentials(
	username, password, encrypt_password, nickname)
	VALUES ('username', 'password', 'encryption-pw', 'snmp1');

INSERT INTO public.snmp_versions(
	version_code, nickname)
	VALUES ('41', 'RSU 4.1');
INSERT INTO public.snmp_versions(
	version_code, nickname)
	VALUES ('1218', 'NTCIP 1218');

INSERT INTO public.rsus(
	geography, milepost, ipv4_address, serial_number, iss_scms_id, primary_route, model, credential_id, snmp_credential_id, snmp_version_id, firmware_version, target_firmware_version)
	VALUES (ST_GeomFromText('POINT(-105.014182 39.740422)'), 1, '10.0.0.180', 'E5672', 'E5672', 'I999', 1, 1, 1, 1, 1, 1), 
	(ST_GeomFromText('POINT(-104.967723 39.918758)'), 2, '10.0.0.78', 'E5321', 'E5321', 'I999', 1, 1, 1, 2, 2, 2);

INSERT INTO public.organizations(
	name)
	VALUES ('Test Org');

INSERT INTO public.roles(
	name)
	VALUES ('admin'), ('operator'), ('user');

INSERT INTO public.rsu_organization(
	rsu_id, organization_id)
	VALUES (1, 1), (2, 1);

-- Replace user with a real gmail to test GCP OAuth2.0 support
INSERT INTO public.users(
	email, first_name, last_name, super_user, receive_error_emails)
	VALUES ('test@gmail.com', 'Test', 'User', '1', '1');

INSERT INTO public.user_organization(
	user_id, organization_id, role_id)
	VALUES (1, 1, 1);

INSERT INTO public.snmp_msgfwd_type(
	name)
	VALUES ('rsuDsrcFwd'), ('rsuReceivedMsg'), ('rsuXmitMsgFwding');
