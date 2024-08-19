ALTER TABLE public.organizations
  ADD COLUMN email character varying(128) COLLATE pg_catalog.default;

DROP TABLE user_email_notification CASCADE;

CREATE SEQUENCE public.user_email_notification_user_email_notification_id_seq
  INCREMENT 1
  START 1
  MINVALUE 1
  MAXVALUE 2147483647
  CACHE 1;

CREATE TABLE IF NOT EXISTS public.user_email_notification
(
  user_email_notification_id integer NOT NULL DEFAULT nextval('user_email_notification_user_email_notification_id_seq'::regclass),
  user_id integer NOT NULL,
  organization_id integer NOT NULL,
  email_type_id integer NOT NULL,
  CONSTRAINT user_email_notification_pkey PRIMARY KEY (user_email_notification_id),
  CONSTRAINT fk_user_id FOREIGN KEY (user_id)
    REFERENCES public.users (user_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION,
  CONSTRAINT fk_organization_id FOREIGN KEY (organization_id)
    REFERENCES public.organizations (organization_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION,
  CONSTRAINT fk_email_type_id FOREIGN KEY (email_type_id)
    REFERENCES public.email_type (email_type_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
);

CREATE SEQUENCE public.organization_email_notification_organization_email_notification_id_seq
  INCREMENT 1
  START 1
  MINVALUE 1
  MAXVALUE 2147483647
  CACHE 1;

CREATE TABLE IF NOT EXISTS public.organization_email_notification
(
  organization_email_notification_id integer NOT NULL DEFAULT nextval('organization_email_notification_organization_email_notification_id_seq'::regclass),
  organization_id integer NOT NULL,
  email_type_id integer NOT NULL,
  CONSTRAINT organization_email_notification_id_pkey PRIMARY KEY (organization_email_notification_id),
  CONSTRAINT fk_organization_id FOREIGN KEY (organization_id)
    REFERENCES public.organizations (organization_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION,
  CONSTRAINT fk_email_type_id FOREIGN KEY (email_type_id)
    REFERENCES public.email_type (email_type_id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION
);