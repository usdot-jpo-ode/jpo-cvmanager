import logging
from common.email_api import EmailApi
import common.pgquery as pgquery
from datetime import datetime, timedelta
from pymongo import MongoClient
import count_metric_environment
from common.keycloak_api import KeycloakServiceAccountApi

message_types = ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]


# Modify the rsu_dict with the specified date range's mongoDB "in" counts for each message type
# The rsu_dict is modified in place
def query_mongo_in_counts(rsu_dict, start_dt, end_dt, mongo_db):
    for type in message_types:
        collection = mongo_db[f"OdeRawEncoded{type.upper()}Json"]
        # Perform mongoDB aggregate query
        agg_result = collection.aggregate(
            [
                {
                    "$match": {
                        "recordGeneratedAt": {
                            "$gte": start_dt,
                            "$lt": end_dt,
                        }
                    }
                },
                {
                    "$group": {
                        "_id": "$metadata.originIp",
                        "count": {"$sum": 1},
                    }
                },
            ]
        )
        for record in agg_result:
            if not record["_id"]:
                continue
            rsu_ip = record["_id"]
            count = record["count"]

            logging.debug(f"{type.title()} In count received for {rsu_ip}: {count}")

            # If the RSU is a part of the organization, add it to the rsu_dict
            if rsu_ip in rsu_dict:
                rsu_dict[rsu_ip]["counts"][type]["in"] = count


# Modify the rsu_dict with the specified date range's mongoDB "out" counts for each message type
# The rsu_dict is modified in place
def query_mongo_out_counts(rsu_dict, start_dt, end_dt, mongo_db):
    for type in message_types:
        collection = mongo_db[f"Ode{type.title()}Json"]
        # Perform mongoDB aggregate query
        agg_result = collection.aggregate(
            [
                {
                    "$match": {
                        "recordGeneratedAt": {
                            "$gte": start_dt,
                            "$lt": end_dt,
                        }
                    }
                },
                {
                    "$group": {
                        "_id": "$metadata.originIp",
                        "count": {"$sum": 1},
                    }
                },
            ]
        )
        for record in agg_result:
            if not record["_id"]:
                continue
            rsu_ip = record["_id"]
            count = record["count"]

            logging.debug(f"{type.title()} Out count received for {rsu_ip}: {count}")

            # If the RSU is a part of the organization, add it to the rsu_dict
            if rsu_ip in rsu_dict:
                rsu_dict[rsu_ip]["counts"][type]["out"] = count


def prepare_org_rsu_dict():
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT o.name org_name, r.ipv4_address, r.primary_route "
        "FROM public.rsu_organization ro "
        "JOIN public.organizations o ON ro.organization_id = o.organization_id "
        "JOIN public.rsus r ON ro.rsu_id = r.rsu_id "
        "ORDER BY o.name, r.primary_route ASC, r.milepost ASC"
        ") as row"
    )

    # Query PostgreSQL for the list of SNMP message forwarding configurations tracked in PostgreSQL
    data = pgquery.query_db(query)

    rsu_dict = {}
    for row in data:
        row = dict(row[0])
        # If the organization name is new to the dictionary, make a new empty object
        if row["org_name"] not in rsu_dict:
            rsu_dict[row["org_name"]] = {}

        rsu_dict[row["org_name"]][row["ipv4_address"]] = {
            "primary_route": row["primary_route"],
            "counts": {},
        }

        for type in message_types:
            rsu_dict[row["org_name"]][row["ipv4_address"]]["counts"][type] = {
                "in": 0,
                "out": 0,
            }

    logging.debug(f"Created RSU dictionary: {rsu_dict}")

    return rsu_dict


def email_daily_counts(
    org_name: str,
    deployment_title: str,
    start_date: datetime,
    end_date: datetime,
    message_type_list: list[str],
    counts: list[dict],
):
    logging.info("Attempting to send the count emails...")
    try:
        kc_api = KeycloakServiceAccountApi(
            endpoint=count_metric_environment.KC_ENDPOINT,
            realm=count_metric_environment.KC_REALM,
            client_id=count_metric_environment.KC_SA_CLIENT_ID,
            client_secret=count_metric_environment.KC_SA_CLIENT_SECRET,
        )
        email_api = EmailApi(
            iapi_base_url=count_metric_environment.IAPI_ENDPOINT, kc_api=kc_api
        )

        email_api.send_message_counts(
            org_name,
            deployment_title,
            start_date,
            end_date,
            message_type_list,
            counts,
        )
    except Exception as e:
        logging.error(e)


def run_daily_emailer():
    client = MongoClient(count_metric_environment.MONGO_DB_URI)
    mongo_db = client[count_metric_environment.MONGO_DB_NAME]

    # Grab today's date and yesterday's date for a 24 hour range
    start_dt = (datetime.now() - timedelta(1)).replace(
        hour=0, minute=0, second=0, microsecond=0
    )
    end_dt = (datetime.now()).replace(hour=0, minute=0, second=0, microsecond=0)

    # Grab the RSU dictionary for each CV Manager organization to build separate reports
    org_rsu_dict = prepare_org_rsu_dict()

    for org_name, rsu_dict in org_rsu_dict.items():
        # Populate rsu_dict with counts from mongoDB
        query_mongo_in_counts(rsu_dict, start_dt, end_dt, mongo_db)
        query_mongo_out_counts(rsu_dict, start_dt, end_dt, mongo_db)

        rsu_counts = [
            {
                "rsu_ip": rsu_ip,
                "counts": data["counts"],
                "primary_route": data["primary_route"],
            }
            for rsu_ip, data in rsu_dict.items()
        ]

        # Send emails through the Intersection API
        email_daily_counts(
            org_name=org_name,
            deployment_title=count_metric_environment.DEPLOYMENT_TITLE,
            start_date=start_dt,
            end_date=end_dt,
            message_type_list=message_types,
            counts=rsu_counts,
        )


if __name__ == "__main__":
    run_daily_emailer()
