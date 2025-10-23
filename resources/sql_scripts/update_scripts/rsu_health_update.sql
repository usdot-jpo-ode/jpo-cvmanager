-- Create rsu_health table
CREATE SEQUENCE public.rsu_health_rsu_health_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

CREATE TABLE IF NOT EXISTS public.rsu_health
(
    rsu_health_id integer NOT NULL DEFAULT nextval('rsu_health_rsu_health_id_seq'::regclass),
    timestamp timestamp without time zone NOT NULL,
    health integer NOT NULL,
    rsu_id integer NOT NULL,
    CONSTRAINT rsu_health_pkey PRIMARY KEY (rsu_health_id),
    CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
		REFERENCES public.rsus (rsu_id) MATCH SIMPLE
		ON UPDATE NO ACTION
		ON DELETE NO ACTION
);