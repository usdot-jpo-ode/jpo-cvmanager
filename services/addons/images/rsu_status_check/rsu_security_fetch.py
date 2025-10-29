import os
import logging
from common.snmp.update_pg.update_rsu_security import (
    UpdatePostgresRsuSecurity,
)


# Pulls the latest RSU security certificate expiration information from all RSUs in the PostgreSQL database
# through SNMP (NTCIP-1218 only) and updates the PostgreSQL database with the latest information
def main():
    # Configure logging based on ENV var or use default if not set
    log_level = os.environ.get("LOGGING_LEVEL", "INFO")
    log_level = "INFO" if log_level == "" else log_level
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    run_service = os.environ.get("RSU_SECURITY_FETCH", "False").lower() == "true"
    if not run_service:
        logging.info("The rsu-security-fetch service is disabled and will not run")
        return

    update_pg_rsu_security = UpdatePostgresRsuSecurity()
    rsu_list = update_pg_rsu_security.get_rsu_list()
    configs = update_pg_rsu_security.get_snmp_configs(rsu_list)
    update_pg_rsu_security.update_postgresql(configs)


if __name__ == "__main__":
    main()
