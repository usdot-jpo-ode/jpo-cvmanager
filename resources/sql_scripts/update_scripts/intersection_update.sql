-- Intersections
CREATE SEQUENCE public.intersections_intersection_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.intersections
(
   intersection_id integer NOT NULL DEFAULT nextval('intersections_intersection_id_seq'::regclass),
   intersection_number character varying(128) NOT NULL,
   ref_pt GEOGRAPHY(POINT, 4326) NOT NULL,
   bbox GEOGRAPHY(POLYGON, 4326),
   intersection_name character varying(128),
   origin_ip inet,
   CONSTRAINT intersection_pkey PRIMARY KEY (intersection_id),
   CONSTRAINT intersection_intersection_number UNIQUE (intersection_number)
);

CREATE SEQUENCE public.intersection_organization_intersection_organization_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.intersection_organization
(
   intersection_organization_id integer NOT NULL DEFAULT nextval('intersection_organization_intersection_organization_id_seq'::regclass),
   intersection_id integer NOT NULL,
   organization_id integer NOT NULL,
   CONSTRAINT intersection_organization_pkey PRIMARY KEY (intersection_organization_id),
   CONSTRAINT fk_intersection_id FOREIGN KEY (intersection_id)
      REFERENCES public.intersections (intersection_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_organization_id FOREIGN KEY (organization_id)
      REFERENCES public.organizations (organization_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE SEQUENCE public.rsu_intersection_rsu_intersection_id_seq
   INCREMENT 1
   START 1
   MINVALUE 1
   MAXVALUE 2147483647
   CACHE 1;

CREATE TABLE IF NOT EXISTS public.rsu_intersection
(
   rsu_intersection_id integer NOT NULL DEFAULT nextval('rsu_intersection_rsu_intersection_id_seq'::regclass),
   rsu_id integer NOT NULL,
   intersection_id integer NOT NULL,
   CONSTRAINT rsu_intersection_pkey PRIMARY KEY (rsu_intersection_id),
   CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
      REFERENCES public.rsus (rsu_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_intersection_id FOREIGN KEY (intersection_id)
      REFERENCES public.intersections (intersection_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);