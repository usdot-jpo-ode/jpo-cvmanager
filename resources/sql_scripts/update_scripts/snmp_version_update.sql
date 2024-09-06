-- Run this SQL update script if you already have a deployed CV Manager PostgreSQL database prior to the SNMP protocol version addition
-- This file will create the 'snmp_protocols' table and add NTCIP 1218 and RSU 4.1 as SNMP protocol versions
-- All RSUs are given the default of RSU 4.1 as their SNMP protocol version using this script

CREATE SEQUENCE public.snmp_protocols_snmp_protocol_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.snmp_protocols
(
   snmp_protocol_id integer NOT NULL DEFAULT nextval('snmp_protocols_snmp_protocol_id_seq'::regclass),
   protocol_code character varying(128) COLLATE pg_catalog.default NOT NULL,
   nickname character varying(128) COLLATE pg_catalog.default NOT NULL,
   CONSTRAINT snmp_protocols_pkey PRIMARY KEY (snmp_protocol_id),
   CONSTRAINT snmp_protocols_nickname UNIQUE (nickname)
);

INSERT INTO public.snmp_protocols(
	protocol_code, nickname)
	VALUES ('41', 'RSU 4.1');
INSERT INTO public.snmp_protocols(
	protocol_code, nickname)
	VALUES ('1218', 'NTCIP 1218');

ALTER TABLE public.rsus
        ADD snmp_protocol_id integer NOT NULL
    DEFAULT (1);

ALTER TABLE public.rsus     
    ADD CONSTRAINT fk_snmp_protocol_id FOREIGN KEY (snmp_protocol_id)
      REFERENCES public.snmp_protocols (snmp_protocol_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION