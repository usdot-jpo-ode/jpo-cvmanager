import os
import logging
import common.pgquery as pgquery
import common.update_rsu_snmp_pg as update_rsu_snmp_pg


def get_rsu_list():
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT rd.rsu_id, rd.ipv4_address, snmp.username AS snmp_username, snmp.password AS snmp_password, sver.version_code AS snmp_version "
        "FROM public.rsus AS rd "
        "LEFT JOIN public.snmp_credentials AS snmp ON snmp.snmp_credential_id = rd.snmp_credential_id "
        "LEFT JOIN public.snmp_versions AS sver ON sver.snmp_version_id = rd.snmp_version_id"
        ") as row"
    )

    # Query PostgreSQL for the list of RSU IPs with SNMP credentials and version
    data = pgquery.query_db(query)

    rsu_list = []
    for row in data:
        rsu_list.append(dict(row[0]))

    return rsu_list


if __name__ == "__main__":
    # Configure logging based on ENV var or use default if not set
    log_level = os.environ.get("LOGGING_LEVEL", "INFO")
    log_level = "INFO" if log_level == "" else log_level
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    run_service = os.environ.get("RSU_SNMP_FETCH", "False").lower() == "true"
    if not run_service:
        logging.info("The rsu-snmp-fetch service is disabled and will not run")
        exit()

    rsu_list = get_rsu_list()
    configs = update_rsu_snmp_pg.get_snmp_configs(rsu_list)
    update_rsu_snmp_pg.update_postgresql(configs)
