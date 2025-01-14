-- Run this SQL update script if you already have a deployed CV Manager PostgreSQL database
-- This file will recreate the firmware tables, make sure to backup any data that may be located
-- in the firmware table before running this.

ALTER TABLE public.rsus
    DROP COLUMN os_version,
    DROP COLUMN firmware_version;

DROP TABLE public.firmware_upgrade_rules;
DROP SEQUENCE public.firmware_upgrade_rules_firmware_upgrade_rule_id_seq;
DROP TABLE public.firmware_images;
DROP SEQUENCE public.firmware_images_firmware_id_seq;
DROP TABLE public.os_upgrade_rules;
DROP SEQUENCE public.os_upgrade_rules_os_upgrade_rule_id_seq;
DROP TABLE public.os_images;
DROP SEQUENCE public.os_images_os_id_seq;

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
    install_package character varying(128) COLLATE pg_catalog.default NOT NULL,
    version character varying(128) COLLATE pg_catalog.default NOT NULL,
    CONSTRAINT firmware_images_pkey PRIMARY KEY (firmware_id),
    CONSTRAINT firmware_images_name UNIQUE (name),
    CONSTRAINT firmware_images_install_package UNIQUE (install_package),
    CONSTRAINT firmware_images_version UNIQUE (version),
    CONSTRAINT fk_model FOREIGN KEY (model)
        REFERENCES public.rsu_models (rsu_model_id) MATCH SIMPLE
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

ALTER TABLE public.rsus
    ADD firmware_version integer,
    ADD target_firmware_version integer,
    CONSTRAINT fk_firmware_version FOREIGN KEY (firmware_version)
        REFERENCES public.firmware_images (firmware_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    ADD CONSTRAINT fk_target_firmware_version FOREIGN KEY (target_firmware_version)
        REFERENCES public.firmware_images (firmware_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;