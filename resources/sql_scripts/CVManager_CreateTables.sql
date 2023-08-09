-- RSU Manager Cloud Run Tables
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE SEQUENCE public.manufacturers_manufacturer_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.manufacturers
(
   manufacturer_id integer NOT NULL DEFAULT nextval('manufacturers_manufacturer_id_seq'::regclass),
   name character varying(128) COLLATE pg_catalog.default NOT NULL,
   CONSTRAINT manufacturers_pkey PRIMARY KEY (manufacturer_id),
   CONSTRAINT manufacturers_name UNIQUE (name)
);

CREATE SEQUENCE public.rsu_models_rsu_model_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.rsu_models
(
   rsu_model_id integer NOT NULL DEFAULT nextval('rsu_models_rsu_model_id_seq'::regclass),
   name character varying(128) COLLATE pg_catalog.default NOT NULL,
   supported_radio character varying(128) COLLATE pg_catalog.default NOT NULL,
   manufacturer integer NOT NULL,
   CONSTRAINT rsu_models_pkey PRIMARY KEY (rsu_model_id),
   CONSTRAINT rsu_models_name UNIQUE (name),
   CONSTRAINT fk_manufacturer FOREIGN KEY (manufacturer)
      REFERENCES public.manufacturers (manufacturer_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE SEQUENCE public.os_images_os_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.os_images
(
   os_id integer NOT NULL DEFAULT nextval('os_images_os_id_seq'::regclass),
   name character varying(128) COLLATE pg_catalog.default NOT NULL,
   model integer NOT NULL,
   install_script character varying(128) COLLATE pg_catalog.default NOT NULL,
   update_image character varying(128) COLLATE pg_catalog.default NOT NULL,
   version character varying(128) COLLATE pg_catalog.default NOT NULL,
   rescue_image character varying(128) COLLATE pg_catalog.default,
   rescue_install_script character varying(128) COLLATE pg_catalog.default,
   CONSTRAINT os_images_pkey PRIMARY KEY (os_id),
   CONSTRAINT os_images_install_script UNIQUE (install_script),
   CONSTRAINT os_images_name UNIQUE (name),
   CONSTRAINT os_images_rescue_image UNIQUE (rescue_image),
   CONSTRAINT os_images_rescue_install_script UNIQUE (rescue_install_script),
   CONSTRAINT os_images_update_image UNIQUE (update_image),
   CONSTRAINT os_images_version UNIQUE (version),
   CONSTRAINT fk_model FOREIGN KEY (model)
      REFERENCES public.rsu_models (rsu_model_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE SEQUENCE public.os_upgrade_rules_os_upgrade_rule_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.os_upgrade_rules
(
   os_upgrade_rule_id integer NOT NULL DEFAULT nextval('os_upgrade_rules_os_upgrade_rule_id_seq'::regclass),
   from_id integer NOT NULL,
   to_id integer NOT NULL,
   CONSTRAINT os_upgrade_rules_pkey PRIMARY KEY (os_upgrade_rule_id),
   CONSTRAINT fk_from_id FOREIGN KEY (from_id)
      REFERENCES public.os_images (os_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_to_id FOREIGN KEY (to_id)
      REFERENCES public.os_images (os_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE SEQUENCE public.firmware_images_firmware_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.firmware_images
(
   firmware_id integer NOT NULL DEFAULT nextval('firmware_images_firmware_id_seq'::regclass),
   name character varying(128) COLLATE pg_catalog.default NOT NULL,
   model integer NOT NULL,
   os_required integer NOT NULL,
   install_script character varying(128) COLLATE pg_catalog.default NOT NULL,
   update_image character varying(128) COLLATE pg_catalog.default NOT NULL,
   version character varying(128) COLLATE pg_catalog.default NOT NULL,
   CONSTRAINT firmware_images_pkey PRIMARY KEY (firmware_id),
   CONSTRAINT firmware_images_install_script UNIQUE (install_script),
   CONSTRAINT firmware_images_name UNIQUE (name),
   CONSTRAINT firmware_images_update_image UNIQUE (update_image),
   CONSTRAINT firmware_images_version UNIQUE (version),
   CONSTRAINT fk_model FOREIGN KEY (model)
      REFERENCES public.rsu_models (rsu_model_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_os_required FOREIGN KEY (os_required)
      REFERENCES public.os_images (os_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE SEQUENCE public.firmware_upgrade_rules_firmware_upgrade_rule_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.firmware_upgrade_rules
(
   firmware_upgrade_rule_id integer NOT NULL DEFAULT nextval('firmware_upgrade_rules_firmware_upgrade_rule_id_seq'::regclass),
   from_id integer NOT NULL,
   to_id integer NOT NULL,
   CONSTRAINT firmware_upgrade_rules_pkey PRIMARY KEY (firmware_upgrade_rule_id),
   CONSTRAINT fk_from_id FOREIGN KEY (from_id)
      REFERENCES public.firmware_images (firmware_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_to_id FOREIGN KEY (to_id)
      REFERENCES public.firmware_images (firmware_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE SEQUENCE public.rsu_credentials_credential_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.rsu_credentials
(
   credential_id integer NOT NULL DEFAULT nextval('rsu_credentials_credential_id_seq'::regclass),
   username character varying(128) COLLATE pg_catalog.default NOT NULL,
   password character varying(128) COLLATE pg_catalog.default NOT NULL,
   nickname character varying(128) COLLATE pg_catalog.default NOT NULL,
   CONSTRAINT rsu_credentials_pkey PRIMARY KEY (credential_id),
   CONSTRAINT rsu_credentials_nickname UNIQUE (nickname)
);

CREATE SEQUENCE public.snmp_credentials_snmp_credential_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.snmp_credentials
(
   snmp_credential_id integer NOT NULL DEFAULT nextval('snmp_credentials_snmp_credential_id_seq'::regclass),
   username character varying(128) COLLATE pg_catalog.default NOT NULL,
   password character varying(128) COLLATE pg_catalog.default NOT NULL,
   nickname character varying(128) COLLATE pg_catalog.default NOT NULL,
   CONSTRAINT snmp_credentials_pkey PRIMARY KEY (snmp_credential_id),
   CONSTRAINT snmp_credentials_nickname UNIQUE (nickname)
);

CREATE SEQUENCE public.snmp_versions_snmp_version_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.snmp_versions
(
   snmp_version_id integer NOT NULL DEFAULT nextval('snmp_versions_snmp_version_id_seq'::regclass),
   version_code character varying(128) COLLATE pg_catalog.default NOT NULL,
   nickname character varying(128) COLLATE pg_catalog.default NOT NULL,
   CONSTRAINT snmp_versions_pkey PRIMARY KEY (snmp_version_id),
   CONSTRAINT snmp_versions_nickname UNIQUE (nickname)
);

CREATE SEQUENCE public.rsus_rsu_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.rsus
(
   rsu_id integer NOT NULL DEFAULT nextval('rsus_rsu_id_seq'::regclass),
   geography geography NOT NULL,
   milepost double precision NOT NULL,
   ipv4_address inet NOT NULL,
   serial_number character varying(128) COLLATE pg_catalog.default NOT NULL,
   iss_scms_id character varying(128) COLLATE pg_catalog.default NOT NULL,
   primary_route character varying(128) COLLATE pg_catalog.default NOT NULL,
   model integer NOT NULL,
   credential_id integer NOT NULL,
   snmp_credential_id integer NOT NULL,
   snmp_version_id integer NOT NULL,
   os_version integer,
   firmware_version integer,
   CONSTRAINT rsu_pkey PRIMARY KEY (rsu_id),
   CONSTRAINT rsu_ipv4_address UNIQUE (ipv4_address),
   CONSTRAINT rsu_milepost_primary_route UNIQUE (milepost, primary_route),
   CONSTRAINT rsu_serial_number UNIQUE (serial_number),
   CONSTRAINT rsu_iss_scms_id UNIQUE (iss_scms_id),
   CONSTRAINT fk_credential_id FOREIGN KEY (credential_id)
      REFERENCES public.rsu_credentials (credential_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_firmware_version FOREIGN KEY (firmware_version)
      REFERENCES public.firmware_images (firmware_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_model FOREIGN KEY (model)
      REFERENCES public.rsu_models (rsu_model_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_os_version FOREIGN KEY (os_version)
      REFERENCES public.os_images (os_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_snmp_credential_id FOREIGN KEY (snmp_credential_id)
      REFERENCES public.snmp_credentials (snmp_credential_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_snmp_version_id FOREIGN KEY (snmp_version_id)
      REFERENCES public.snmp_versions (snmp_version_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE SEQUENCE public.ping_ping_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.ping
(
   ping_id integer NOT NULL DEFAULT nextval('ping_ping_id_seq'::regclass),
   timestamp timestamp without time zone NOT NULL,
   result bit(1) NOT NULL,
   rsu_id integer NOT NULL,
   CONSTRAINT ping_pkey PRIMARY KEY (ping_id),
   CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
      REFERENCES public.rsus (rsu_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE SEQUENCE public.roles_role_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.roles
(
   role_id integer NOT NULL DEFAULT nextval('roles_role_id_seq'::regclass),
   name character varying(128) COLLATE pg_catalog.default NOT NULL,
   CONSTRAINT roles_pkey PRIMARY KEY (role_id),
   CONSTRAINT roles_name UNIQUE (name)
);

CREATE SEQUENCE public.users_user_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.users
(
   user_id integer NOT NULL DEFAULT nextval('users_user_id_seq'::regclass),
   email character varying(128) COLLATE pg_catalog.default NOT NULL,
   first_name character varying(128) NOT NULL,
   last_name character varying(128) NOT NULL,
   super_user bit(1) NOT NULL,
   CONSTRAINT users_pkey PRIMARY KEY (user_id),
   CONSTRAINT users_email UNIQUE (email)
);

CREATE SEQUENCE public.organizations_organization_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.organizations
(
   organization_id integer NOT NULL DEFAULT nextval('organizations_organization_id_seq'::regclass),
   name character varying(128) COLLATE pg_catalog.default NOT NULL,
   CONSTRAINT organizations_pkey PRIMARY KEY (organization_id),
   CONSTRAINT organizations_name UNIQUE (name)
);

CREATE SEQUENCE public.user_organization_user_organization_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.user_organization
(
   user_organization_id integer NOT NULL DEFAULT nextval('user_organization_user_organization_id_seq'::regclass),
   user_id integer NOT NULL,
   organization_id integer NOT NULL,
   role_id integer NOT NULL,
   CONSTRAINT user_organization_pkey PRIMARY KEY (user_organization_id),
   CONSTRAINT fk_user_id FOREIGN KEY (user_id)
      REFERENCES public.users (user_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_organization_id FOREIGN KEY (organization_id)
      REFERENCES public.organizations (organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_role_id FOREIGN KEY (role_id)
      REFERENCES public.roles (role_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE SEQUENCE public.rsu_organization_rsu_organization_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.rsu_organization
(
   rsu_organization_id integer NOT NULL DEFAULT nextval('rsu_organization_rsu_organization_id_seq'::regclass),
   rsu_id integer NOT NULL,
   organization_id integer NOT NULL,
   CONSTRAINT rsu_organization_pkey PRIMARY KEY (rsu_organization_id),
   CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
      REFERENCES public.rsus (rsu_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_organization_id FOREIGN KEY (organization_id)
      REFERENCES public.organizations (organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS public.map_info
(
   ipv4_address inet NOT NULL,
   geojson json NOT NULL,
   date character varying(64) COLLATE pg_catalog.default,
   CONSTRAINT map_info_pkey PRIMARY KEY (ipv4_address),
   CONSTRAINT fk_ipv4_address FOREIGN KEY (ipv4_address)
      REFERENCES public.rsus (ipv4_address) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE VIEW public.rsu_organization_name AS
SELECT ro.rsu_id, org.name
FROM public.rsu_organization AS ro
JOIN public.organizations AS org ON ro.organization_id = org.organization_id;

-- Create scms_health table
CREATE SEQUENCE public.scms_health_scms_health_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.scms_health
(
   scms_health_id integer NOT NULL DEFAULT nextval('scms_health_scms_health_id_seq'::regclass),
   timestamp timestamp without time zone NOT NULL,
   health bit(1) NOT NULL,
   expiration timestamp without time zone,
   rsu_id integer NOT NULL,
   CONSTRAINT scms_health_pkey PRIMARY KEY (scms_health_id),
   CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
		REFERENCES public.rsus (rsu_id) MATCH SIMPLE
		ON UPDATE NO ACTION
		ON DELETE NO ACTION
);

CREATE SEQUENCE public.snmp_versions_snmp_version_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.snmp_versions
(
   snmp_version_id integer NOT NULL DEFAULT nextval('snmp_versions_snmp_version_id_seq'::regclass),
   version_code character varying(128) COLLATE pg_catalog.default NOT NULL,
   nickname character varying(128) COLLATE pg_catalog.default NOT NULL,
   CONSTRAINT snmp_versions_pkey PRIMARY KEY (snmp_version_id),
   CONSTRAINT snmp_versions_nickname UNIQUE (nickname)
);