import logging
import common.pgquery as pgquery
import common.snmp.ntcip1218.rsu_status as ntcip1218_health
from common.snmp.update_pg.update_pg_snmp import UpdatePostgresSnmpAbstractClass
from datetime import datetime, timezone
from multiprocessing import Pool, cpu_count


class UpdatePostgresRsuHealth(UpdatePostgresSnmpAbstractClass):

    def insert_config_list(self, snmp_config_list):
        query = "INSERT INTO public.rsu_health(" "timestamp, health, rsu_id) " "VALUES"

        for snmp_config in snmp_config_list:
            query += f" ('{snmp_config['timestamp']}', {snmp_config['health']}, {snmp_config['rsu_id']}),"

        pgquery.write_db(query[:-1])

    def update_postgresql(self, rsu_snmp_configs_obj, subset=False):
        snmp_config_list = []
        for rsu_id, config in rsu_snmp_configs_obj.items():
            if config is None:
                continue

            # Create a dictionary to represent the RSU health data
            rsu_scms_data = {
                "timestamp": config["timestamp"].strftime("%Y-%m-%d %H:00"),
                "health": config["health"],
                "rsu_id": rsu_id,
            }
            snmp_config_list.append(rsu_scms_data)

        if len(snmp_config_list) > 0:
            self.insert_config_list(snmp_config_list)
        else:
            logging.info("No RSU health data to update in PostgreSQL")

    def process_rsu(self, rsu):
        # Process a single RSU
        snmp_creds = {
            "username": rsu["snmp_username"],
            "password": rsu["snmp_password"],
            "encrypt_pw": rsu["snmp_encrypt_pw"],
        }

        if rsu["snmp_version"] != "1218":
            logging.info(
                f"Unsupported SNMP version for collecting security data for {rsu['rsu_id']}"
            )
            # Return unknown status if the SNMP version is not 1218
            return rsu["rsu_id"], 5

        response, code = ntcip1218_health.get(rsu["ipv4_address"], snmp_creds)

        if code != 200:
            logging.info(f"SNMP response was unsuccessful for {rsu['rsu_id']}")

        # Create an object to represent all RSU health data for PostgreSQL
        config = {
            "timestamp": datetime.now(timezone.utc),
            "health": response["RsuStatus"],
        }

        return rsu["rsu_id"], config

    def get_snmp_configs(self, rsu_list):
        config_obj = {}

        # Use a multiprocessing pool to process RSUs in parallel
        with Pool(processes=cpu_count()) as pool:
            results = pool.map(self.process_rsu, rsu_list)

        # Collect results into the config_obj dictionary
        for rsu_id, config in results:
            config_obj[rsu_id] = config

        return config_obj
