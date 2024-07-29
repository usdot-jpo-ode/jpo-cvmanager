ALTER TABLE public.rsus 
    RENAME COLUMN snmp_version_id TO snmp_protocol_id

ALTER TABLE public.rsus     
    DROP CONSTRAINT fk_snmp_version_id

ALTER TABLE public.rsus     
    ADD CONSTRAINT fk_snmp_version_id FOREIGN KEY (snmp_protocol_id)
      REFERENCES public.snmp_versions (snmp_version_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION