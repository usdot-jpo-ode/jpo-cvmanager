import os
import logging
import gen_email
import smtplib
import ssl
import common.pgquery as pgquery
from datetime import datetime, timedelta
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from pymongo import MongoClient

message_types = ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]

client = MongoClient(os.getenv("MONGO_DB_URI"))
mongo_db = client[os.getenv("MONGO_DB_NAME")]


# Modify the rsu_dict with the specified date range's mongoDB "in" counts for each message type
# The rsu_dict is modified in place
def query_mongo_in_counts(rsu_dict, start_dt, end_dt):
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
                        "_id": f"${type.title()}MessageContent.metadata.originRsu",
                        "count": {"$sum": 1},
                    }
                },
            ]
        )
        for record in agg_result:
            if not record["_id"]:
                continue
            rsu_ip = record["_id"][0]
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
def query_mongo_out_counts(rsu_dict, start_dt, end_dt):
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
                        "_id": f"$metadata.originIp",
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


def prepare_rsu_dict():
    query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT ipv4_address, primary_route "
        "FROM public.rsus "
        "ORDER BY primary_route ASC, milepost ASC"
        ") as row"
    )

    # Query PostgreSQL for the list of SNMP message forwarding configurations tracked in PostgreSQL
    data = pgquery.query_db(query)

    rsu_dict = {}
    for row in data:
        row = dict(row[0])
        rsu_dict[row["ipv4_address"]] = {
            "primary_route": row["primary_route"],
            "counts": {},
        }
        for type in message_types:
            rsu_dict[row["ipv4_address"]]["counts"][type] = {"in": 0, "out": 0}
    logging.debug(f"Created RSU dictionary: {rsu_dict}")

    return rsu_dict


def email_daily_counts(email_body):
    sender_email = os.environ["SMTP_EMAIL"]
    receiver_emails = os.environ["SMTP_EMAIL_RECIPIENTS"]

    logging.info("Attempting to send the count emails...")
    try:
        message = MIMEMultipart()
        message["Subject"] = f"{str(os.environ['DEPLOYMENT_TITLE']).upper()} Counts"
        message["From"] = sender_email
        message["To"] = receiver_emails

        message.attach(MIMEText(email_body, "html"))

        context = ssl._create_unverified_context()
        smtp = smtplib.SMTP(os.environ["SMTP_SERVER_IP"], 587)
        smtp.starttls(context=context)
        smtp.ehlo()
        smtp.login(os.environ["SMTP_USERNAME"], os.environ["SMTP_PASSWORD"])
        smtp.sendmail(
            sender_email,
            receiver_emails.split(","),
            message.as_string(),
        )
        smtp.quit()

        logging.info("Email sent successfully")
    except Exception as e:
        logging.error(e)


def run_daily_emailer():
    rsu_dict = prepare_rsu_dict()

    # Grabs today's date and yesterday's date for a 24 hour range
    start_dt = (datetime.now() - timedelta(1)).replace(
        hour=0, minute=0, second=0, microsecond=0
    )
    end_dt = (datetime.now()).replace(hour=0, minute=0, second=0, microsecond=0)

    # Populate rsu_dict with counts from mongoDB
    query_mongo_in_counts(rsu_dict, start_dt, end_dt)
    query_mongo_out_counts(rsu_dict, start_dt, end_dt)

    # Generate the email content with the populated rsu_dict
    email_body = gen_email.generate_email_body(rsu_dict, start_dt, end_dt)
    email_daily_counts(email_body)
