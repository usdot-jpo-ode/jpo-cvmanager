import os
import logging
import gen_email
from common.emailSender import EmailSender
import common.pgquery as pgquery
from common.email_util import get_email_list
from datetime import datetime, timedelta
from pymongo import MongoClient

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

            # If a RSU that is not in PostgreSQL has counts recorded, add it to the rsu_dict and populate zeroes
            if rsu_ip not in rsu_dict:
                rsu_dict[rsu_ip] = {
                    "primary_route": "Unknown",
                    "counts": {},
                }
                for t in message_types:
                    rsu_dict[rsu_ip]["counts"][t] = {"in": 0, "out": 0}

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

            # If a RSU that is not in PostgreSQL has counts recorded, add it to the rsu_dict and populate zeroes
            if rsu_ip not in rsu_dict:
                rsu_dict[rsu_ip] = {
                    "primary_route": "Unknown",
                    "counts": {},
                }
                for t in message_types:
                    rsu_dict[rsu_ip]["counts"][t] = {"in": 0, "out": 0}

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


def email_daily_counts(org_name, email_body):
    logging.info("Attempting to send the count emails...")
    try:
        email_addresses = get_email_list("Daily Message Counts", org_name)

        for email_address in email_addresses:
            emailSender = EmailSender(
                os.environ["SMTP_SERVER_IP"],
                587,
            )
            emailSender.send(
                sender=os.environ["SMTP_EMAIL"],
                recipient=email_address,
                subject=f"{org_name} {str(os.environ['DEPLOYMENT_TITLE'])} Counts",
                message=email_body,
                replyEmail="",
                username=os.environ["SMTP_USERNAME"],
                password=os.environ["SMTP_PASSWORD"],
                pretty=True,
            )
    except Exception as e:
        logging.error(e)


def run_daily_emailer():
    client = MongoClient(os.getenv("MONGO_DB_URI"))
    mongo_db = client[os.getenv("MONGO_DB_NAME")]

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

        # Generate the email content with the populated rsu_dict
        email_body = gen_email.generate_email_body(
            org_name, rsu_dict, start_dt, end_dt, message_types
        )
        email_daily_counts(org_name, email_body)


if __name__ == "__main__":
    run_daily_emailer()
