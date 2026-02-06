ALTER TABLE public.obu_ota_requests 
ADD CONSTRAINT obu_ota_requests_pkey PRIMARY KEY (request_id);

ALTER TABLE public.iss_keys 
ADD CONSTRAINT iss_keys_pkey PRIMARY KEY (iss_key_id);