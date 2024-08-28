CREATE SEQUENCE public.obu_ota_request_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;
   

CREATE TABLE IF NOT EXISTS public.obu_ota_requests (
   request_id integer NOT NULL DEFAULT nextval('obu_ota_request_id_seq'::regclass),
	obu_sn character varying(128) NOT NULL,
	request_datetime timestamp NOT NULL,
	origin_ip inet NOT NULL,
   obu_firmware_version varchar(128) NOT NULL,
   requested_firmware_version varchar(128) NOT NULL,
	error_status bit(1) NOT NULL,
   error_message varchar(128) NOT NULL,
   manufacturer int4 NOT NULL,
	CONSTRAINT fk_manufacturer FOREIGN KEY (manufacturer) REFERENCES public.manufacturers(manufacturer_id)
);