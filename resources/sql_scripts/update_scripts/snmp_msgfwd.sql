-- Run this SQL update script if you already have a deployed CV Manager PostgreSQL database prior to the SNMP snmp_msgfwd_config table
-- This file will create the 'snmp_msgfwd_type' and 'snmp_msgfwd_config' tables and add NTCIP 1218 and RSU 4.1 table names to the types table

-- Create snmp_msgfwd_type table
CREATE SEQUENCE public.snmp_msgfwd_type_id_seq
  INCREMENT 1
  START 1
  MINVALUE 1
  MAXVALUE 2147483647
  CACHE 1;

CREATE TABLE IF NOT EXISTS public.snmp_msgfwd_type
(
  snmp_msgfwd_type_id integer NOT NULL DEFAULT nextval('snmp_msgfwd_type_id_seq'::regclass),
  name character varying(128) COLLATE pg_catalog.default NOT NULL,
  CONSTRAINT snmp_msgfwd_type_pkey PRIMARY KEY (snmp_msgfwd_type_id),
  CONSTRAINT snmp_msgfwd_type_name UNIQUE (name)
);

-- Create snmp_msgfwd_config table
CREATE TABLE IF NOT EXISTS public.snmp_msgfwd_config
(
  rsu_id integer NOT NULL,
  msgfwd_type integer NOT NULL,
  snmp_index integer NOT NULL,
  message_type character varying(128) COLLATE pg_catalog.default NOT NULL,
  dest_ipv4 inet NOT NULL,
  dest_port integer NOT NULL,
  start_datetime timestamp without time zone NOT NULL,
  end_datetime timestamp without time zone NOT NULL,
  active bit(1) NOT NULL,
  CONSTRAINT snmp_msgfwd_config_pkey PRIMARY KEY (rsu_id, msgfwd_type, snmp_index),
  CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
    REFERENCES public.rsus (rsu_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
  CONSTRAINT fk_msgfwd_type FOREIGN KEY (msgfwd_type)
    REFERENCES public.snmp_msgfwd_type (snmp_msgfwd_type_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
);

-- Adding RSU 4.1 and NTCIP 1218 message forwarding tables
INSERT INTO public.snmp_msgfwd_type(
	name)
	VALUES ('rsuDsrcFwd'), ('rsuReceivedMsg'), ('rsuXmitMsgFwding');
