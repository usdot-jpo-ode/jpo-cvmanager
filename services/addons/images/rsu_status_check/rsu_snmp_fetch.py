import os
import logging
import common.pgquery as pgquery
import rsufwdsnmpwalk


def iterate_snmpwalk(rsu_list):
    config_obj = {}

    logging.info(rsu_list)
    for rsu in rsu_list:
        request = {
            "rsu_ip": rsu["ipv4_address"],
            "snmp_version": rsu["snmp_version"],
            "snmp_creds": {
                "username": rsu["snmp_username"],
                "password": rsu["snmp_password"],
            },
        }
        response, code = rsufwdsnmpwalk.get(request)

        if code == 200:
            logging.info(response)
        else:
            logging.error(response)

    return config_obj


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
        row = dict(row[0])
        logging.info(row)
        rsu_list.append(row)

    return rsu_list


if __name__ == "__main__":
    # Configure logging based on ENV var or use default if not set
    log_level = os.environ.get("LOGGING_LEVEL", "INFO")
    logging.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    run_service = os.environ.get("RSU_SNMP_FETCH", "False").lower() == "true"
    if not run_service:
        logging.info("The rsu-snmp-fetch service is disabled and will not run")
        exit()

    rsu_list = get_rsu_list()
    iterate_snmpwalk(rsu_list)
