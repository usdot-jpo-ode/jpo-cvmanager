ALTER TABLE public.snmp_credentials
    ADD COLUMN encrypt_password character varying(128) COLLATE pg_catalog.default;