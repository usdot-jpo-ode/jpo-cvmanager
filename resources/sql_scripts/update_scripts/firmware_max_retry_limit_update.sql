-- Run this SQL update script if you already have a deployed CV Manager PostgreSQL database

CREATE TABLE IF NOT EXISTS public.consecutive_firmware_upgrade_failures
(
   rsu_id integer NOT NULL,
   consecutive_failures integer NOT NULL,
   CONSTRAINT consecutive_firmware_upgrade_failures_pkey PRIMARY KEY (rsu_id),
   CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
      REFERENCES public.rsus (rsu_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

CREATE TABLE IF NOT EXISTS public.max_retry_limit_reached_instances
(
   rsu_id integer NOT NULL,
   reached_at timestamp without time zone NOT NULL,
   target_firmware_version integer NOT NULL,
   CONSTRAINT max_retry_limit_reached_instances_pkey PRIMARY KEY (rsu_id, reached_at),
   CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
      REFERENCES public.rsus (rsu_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
   CONSTRAINT fk_target_firmware_version FOREIGN KEY (target_firmware_version)
      REFERENCES public.firmware_images (firmware_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);
