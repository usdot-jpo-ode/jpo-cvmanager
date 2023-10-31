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

INSERT INTO public.snmp_versions(
	version_code, nickname)
	VALUES ('4.1', '4.1');
INSERT INTO public.snmp_versions(
	version_code, nickname)
	VALUES ('12.18', '12.18');

ALTER TABLE public.rsus
        ADD snmp_version_id integer NOT NULL
    DEFAULT (1);

ALTER TABLE public.rsus     
    ADD CONSTRAINT fk_snmp_version_id FOREIGN KEY (snmp_version_id)
      REFERENCES public.snmp_versions (snmp_version_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION