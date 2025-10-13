import logging
from common.snmp.update_pg.update_rsu_message_forward import (
    UpdatePostgresRsuMessageForward,
)
from addons.images.rsu_status_check import environment
from common import common_environment


# Pulls the latest message forwarding configuration information from all RSUs in the PostgreSQL database
# through SNMP and updates the PostgreSQL database with the latest information
def main():
    common_environment.configure_logging()

    if not environment.RSU_MSGFWD_FETCH:
        logging.info("The rsu-msgfwd-fetch service is disabled and will not run")
        return

    update_pg_rsu_mf = UpdatePostgresRsuMessageForward()
    rsu_list = update_pg_rsu_mf.get_rsu_list()
    configs = update_pg_rsu_mf.get_snmp_configs(rsu_list)
    update_pg_rsu_mf.update_postgresql(configs)


if __name__ == "__main__":
    main()
