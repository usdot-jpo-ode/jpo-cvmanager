from datetime import datetime
import requests
import logging
import os
import iss_token
import common.pgquery as pgquery
from dataclasses import dataclass, field
from typing import Dict


# Set up logging
logger = logging.getLogger(__name__)

@dataclass
class RsuDataWrapper:
    rsu_data: Dict[str, Dict[str, str]] = field(default_factory=dict)

    def __init__(self, rsu_data):
        self.rsu_data = rsu_data

    def get_dict(self):
        return self.rsu_data

    def set_provisioner_company(self, scms_id, provisioner_company):
        self.rsu_data[scms_id]["provisionerCompany"] = provisioner_company
    
    def set_entity_type(self, scms_id, entity_type):
        self.rsu_data[scms_id]["entityType"] = entity_type

    def set_project_id(self, scms_id, project_id):
        self.rsu_data[scms_id]["project_id"] = project_id

    def set_device_health(self, scms_id, device_health):
        self.rsu_data[scms_id]["deviceHealth"] = device_health

    def set_expiration(self, scms_id, expiration):
        self.rsu_data[scms_id]["expiration"] = expiration


def get_rsu_data() -> RsuDataWrapper:
    """Get RSU data from PostgreSQL and return it in a wrapper object"""
    
    result = {}
    query = (
        "SELECT jsonb_build_object('rsu_id', rsu_id, 'iss_scms_id', iss_scms_id) "
        "FROM public.rsus "
        "WHERE iss_scms_id IS NOT NULL "
        "ORDER BY rsu_id"
    )
    data = pgquery.query_db(query)

    logger.debug("Parsing results...")
    for point in data:
        point_dict = dict(point[0])
        result[point_dict["iss_scms_id"]] = {"rsu_id": point_dict["rsu_id"]}

    return RsuDataWrapper(result)


def get_scms_status_data():
    """Get SCMS status data from ISS and return it as a dictionary"""

    rsu_data = get_rsu_data()

    # Create GET request headers
    iss_headers = {}
    iss_headers["x-api-key"] = iss_token.get_token()

    # Create the GET request string
    iss_base = os.environ["ISS_SCMS_VEHICLE_REST_ENDPOINT"]
    project_id = os.environ["ISS_PROJECT_ID"]
    page_size = 200
    page = 0
    messages_processed = 0

    # Loop through all pages of enrolled devices
    while True:
        iss_request = iss_base + "?pageSize={}&page={}&project_id={}".format(
            page_size, page, project_id
        )
        logger.debug("GET: " + iss_request)
        response = requests.get(iss_request, headers=iss_headers)
        enrollment_list = response.json()["data"]

        if len(enrollment_list) == 0:
            break

        # Loop through each device on current page
        for enrollment_status in enrollment_list:
            es_id = enrollment_status["_id"]
            if es_id in rsu_data.get_dict():
                rsu_data.set_provisioner_company(es_id, enrollment_status["provisionerCompany_id"])
                rsu_data.set_entity_type(es_id, enrollment_status["entityType"])
                rsu_data.set_project_id(es_id, enrollment_status["project_id"])
                rsu_data.set_device_health(es_id, enrollment_status["deviceHealth"])

                # If the device has yet to download its first set of certs, set the expiration time to when it was enrolled
                if "authorizationCertInfo" in enrollment_status["enrollments"][0]:
                    rsu_data.set_expiration(es_id, enrollment_status["enrollments"][0]["authorizationCertInfo"]["expireTimeOfLatestDownloadedCert"])
                else:
                    rsu_data.set_expiration(es_id, None)

            messages_processed = messages_processed + 1

        page = page + 1

    logger.info("Processed {} messages".format(messages_processed))
    return rsu_data.get_dict()


def insert_scms_data(data):
    logger.info("Inserting SCMS data into PostgreSQL...")
    now_ts = datetime.strftime(datetime.now(), "%Y-%m-%dT%H:%M:%S.000Z")

    query = (
        'INSERT INTO public.scms_health("timestamp", health, expiration, rsu_id) VALUES'
    )
    for value in data.values():
        if validate_scms_data(value) is False:
            continue

        health = "1" if value["deviceHealth"] == "Healthy" else "0"
        if value["expiration"]:
            query = (
                query
                + f" ('{now_ts}', '{health}', '{value['expiration']}', {value['rsu_id']}),"
            )
        else:
            query = query + f" ('{now_ts}', '{health}', NULL, {value['rsu_id']}),"

    query = query[:-1] # remove comma
    pgquery.write_db(query)
    logger.info(
        "SCMS data inserted {} messages into PostgreSQL...".format(len(data.values()))
    )

def validate_scms_data(value):
    """Validate the SCMS data
    
    Args:
        value (dict): SCMS data
    """

    try:
        value["rsu_id"]
    except KeyError as e:
        logger.warning("rsu_id not found in data, is it real data? exception: {}".format(e))
        return False

    try:
        value["deviceHealth"]
    except KeyError as e:
        logger.warning("deviceHealth not found in data for RSU with id {}, is it real data? exception: {}".format(value["rsu_id"], e))
        return False

    try:
        value["expiration"]
    except KeyError as e:
        logger.warning("expiration not found in data for RSU with id {}, is it real data? exception: {}".format(value["rsu_id"], e))
        return False
    
    return True


if __name__ == "__main__":
    # Configure logging based on ENV var or use default if not set
    log_level = (
        "INFO" if "LOGGING_LEVEL" not in os.environ else os.environ["LOGGING_LEVEL"]
    )
    logger.basicConfig(format="%(levelname)s:%(message)s", level=log_level)

    scms_statuses = get_scms_status_data()
    insert_scms_data(scms_statuses)