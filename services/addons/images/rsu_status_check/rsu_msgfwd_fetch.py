import os
import logging
from common.snmp.update_pg.update_rsu_message_forward import (
    UpdatePostgresRsuMessageForward,
)


# Pulls the latest message forwarding configuration information from all RSUs in the PostgreSQL database
# through SNMP and updates the PostgreSQL database with the latest information
if __name__ == "__main__":
    # Configure logging based on ENV var or use default if not set
    log_level = os.environ.get("LOGGING_LEVEL", "INFO")
    log_level = "INFO" if log_level == "" else log_level
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    run_service = os.environ.get("RSU_MSGFWD_FETCH", "False").lower() == "true"
    if not run_service:
        logging.info("The rsu-msgfwd-fetch service is disabled and will not run")
        exit()

    update_pg_rsu_mf = UpdatePostgresRsuMessageForward()
    rsu_list = update_pg_rsu_mf.get_rsu_list()
    configs = update_pg_rsu_mf.get_snmp_configs(rsu_list)
    update_pg_rsu_mf.update_postgresql(configs)
