import logging
import common.pgquery as pgquery
import common.snmp.ntcip1218.rsu_status as ntcip1218_health
from common.snmp.update_pg.update_pg_snmp import UpdatePostgresSnmpAbstractClass
from datetime import datetime, timezone
from multiprocessing import Pool, cpu_count


class UpdatePostgresRsuHealth(UpdatePostgresSnmpAbstractClass):
    """
    UpdatePostgresRsuHealth is a class that manages the synchronization of RSU health statuses collected via SNMP
    between the CV Manager PostgreSQL database and the RSUs. It provides methods to fetch configurations directly from
    RSUs in order to insert and update SNMP configurations stored in PostgreSQL.
    """

    def insert_config_list(self, snmp_config_list):
        """
        Inserts a list of SNMP RSU health statuses into the PostgreSQL database.
        """
        query = "INSERT INTO public.rsu_health(" "timestamp, health, rsu_id) " "VALUES"

        for snmp_config in snmp_config_list:
            query += f" ('{snmp_config['timestamp']}', {snmp_config['health']}, {snmp_config['rsu_id']}),"

        pgquery.write_db(query[:-1])

    def update_postgresql(self, rsu_snmp_configs_obj, subset=False):
        """
        Synchronizes the SNMP RSU statuses between the PostgreSQL database and the provided RSU
        configurations. Handles additions but no deletions are done for historical analysis.
        """
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
        """
        Processes a single RSU to retrieve its health status via SNMP.
        Returns a tuple of (rsu_id, health_config_dict) where health_config_dict contains the timestamp and health status.
        If the SNMP version is unsupported or the SNMP request fails, returns (rsu_id, 5) to indicate unknown status.
        """
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
            # Return unknown status if the SNMP request fails
            return rsu["rsu_id"], 5

        # Create an object to represent all RSU health data for PostgreSQL
        config = {
            "timestamp": datetime.now(timezone.utc),
            "health": response["RsuStatus"],
        }

        return rsu["rsu_id"], config

    def get_snmp_configs(self, rsu_list):
        """
        Retrieves SNMP configuration objects for a list of RSUs in parallel.
        """
        config_obj = {}

        # Use a multiprocessing pool to process RSUs in parallel
        with Pool(processes=cpu_count()) as pool:
            results = pool.map(self.process_rsu, rsu_list)

        # Collect results into the config_obj dictionary
        for rsu_id, config in results:
            config_obj[rsu_id] = config

        return config_obj
