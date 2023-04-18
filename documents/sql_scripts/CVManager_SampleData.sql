-- Sample data to test CV Manager locally or in a new deployment
INSERT INTO public.manufacturers(name) 
  VALUES ('Kapsch'), ('Commsignia'), ('Yunex');

INSERT INTO public.rsu_models(
	rsu_model_id, name, supported_radio, manufacturer)
	VALUES ('RIS-9260', 'DSRC,C-V2X', 1), ('ITS-RS4-M', 'DSRC,C-V2X', 2), ('RSU2X US', 'DSRC,C-V2X', 3);



INSERT INTO public.rsus(
	geography, milepost, ipv4_address, serial_number, primary_route, model, credential_id, snmp_credential_id, iss_scms_id)
	VALUES (?, 1, '10.0.0.1', 'E5672', 'I999', 2, 1, 1, 'E5672');