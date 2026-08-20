-- Add the security column to the snmp_msgfwd_config table
ALTER TABLE public.snmp_msgfwd_config
  ADD COLUMN security bit(1);

-- Populate existing rows with '0' since the CV Manager defaulted to not including security headers in prior versions
UPDATE public.snmp_msgfwd_config SET security = '0';
ALTER TABLE public.snmp_msgfwd_config ALTER COLUMN security SET NOT NULL;