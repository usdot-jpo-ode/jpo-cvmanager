-- Add the rsu_options table
CREATE TABLE IF NOT EXISTS public.rsu_options (
    rsu_id integer NOT NULL,
    tim_deposit boolean NOT NULL DEFAULT FALSE,
    snmp_monitoring boolean NOT NULL DEFAULT FALSE,
    CONSTRAINT rsu_options_pkey PRIMARY KEY (rsu_id),
    CONSTRAINT fk_rsu_id FOREIGN KEY (rsu_id)
        REFERENCES public.rsus (rsu_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Populate existing rows for tim_deposit
INSERT INTO public.rsu_options (rsu_id, tim_deposit, snmp_monitoring)
SELECT rsu_id, FALSE, FALSE FROM public.rsus
ON CONFLICT (rsu_id) DO NOTHING;