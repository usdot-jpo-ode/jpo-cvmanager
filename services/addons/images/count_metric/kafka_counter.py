from confluent_kafka import Consumer, KafkaError, KafkaException
from google.cloud import bigquery
from apscheduler.schedulers.background import BackgroundScheduler
from datetime import timedelta
from datetime import datetime
from dateutil import parser
import pymongo
import os
import json
import copy
import logging


# Class for reading messages of a Kafka topic and counting them based off
# their originIp.
# - thread_id: ID used for logging to print out readable logs
# - message_type: Message type used to create Kafka topic name
# - rsu_location_dict: Dictionary containing all RSU IPs with associated road names
# - rsu_count_dict: Dictionary for keeping track of the counts pre-mapped with RSU IPs
# - rsu_count_dict_zero: What an empty dictionary looks like for proper memory resetting
# - type: Which Kafka topic to listen to, 0: data in, 1: data out
class KafkaMessageCounter:
    def __init__(
        self,
        thread_id,
        message_type,
        rsu_location_dict,
        rsu_count_dict,
        rsu_count_dict_zero,
        type,
    ):
        self.thread_id = thread_id
        self.message_type = message_type
        self.rsu_location_dict = rsu_location_dict
        self.rsu_count_dict = rsu_count_dict
        self.rsu_count_dict_zero = rsu_count_dict_zero
        self.type = type
        if os.getenv("DESTINATION_DB") == "BIGQUERY":
            self.bq_client = bigquery.Client()
        elif os.getenv("DESTINATION_DB") == "MONGODB":
            if os.getenv("MONGO_DB_URI"):
                self.mongo_client = pymongo.MongoClient(os.getenv("MONGO_DB_URI"))
            else:
                logging.error(
                    'Database is set to MongoDB however, the "MONGO_DB_URI" environment variable is not specified.'
                )

    # Pushes a count to the bigquery RSU count table
    def write_bigquery(self, query_values):
        if self.type == 0:
            tablename = os.getenv("KAFKA_BIGQUERY_TABLENAME")
        else:
            tablename = os.getenv("PUBSUB_BIGQUERY_TABLENAME")

        query = (
            f"INSERT INTO `{tablename}`(RSU, Road, Date, Type, Count) "
            f"VALUES {query_values}"
        )

        query_job = self.bq_client.query(query)
        # .result() ensures the Python script waits for this request to finish before moving on
        query_job.result()
        logging.info(
            f"{self.thread_id}: Kafka insert for {self.message_type} succeeded"
        )

    # Pushes a count to the mongo RSU count collection
    def write_mongo(self, documents):
        if self.type == 0:
            collection_name = os.getenv("INPUT_COUNTS_MONGO_COLLECTION_NAME")
        else:
            collection_name = os.getenv("OUTPUT_COUNTS_MONGO_COLLECTION_NAME")

        collection = self.mongo_client[os.getenv("MONGO_DB_NAME")][collection_name]

        result = collection.insert_many(documents)

        if result.acknowledged:
            logging.info(
                f"{self.thread_id}: Kafka insert for {self.message_type} succeeded"
            )
        else:
            logging.error(
                f"{self.thread_id}: The write_mongo method to MongoDB was not acknowledged for {self.message_type.upper()}"
            )

    def push_metrics(self):
        current_counts = copy.deepcopy(self.rsu_count_dict)
        self.rsu_count_dict = copy.deepcopy(self.rsu_count_dict_zero)
        period = datetime.now() - timedelta(hours=1)
        period = datetime.strftime(period, "%Y-%m-%d %H:%M:%S")

        logging.info(f"{self.thread_id}: Creating metrics...")
        if os.getenv("DESTINATION_DB") == "BIGQUERY":
            query_values = ""
            for road, rsu_counts in current_counts.items():
                for ip, count in rsu_counts.items():
                    query_values += f"('{ip}', '{road}', '{period}', '{self.message_type.upper()}', {count}), "

            try:
                if len(query_values) > 0:
                    self.write_bigquery(query_values[:-2])
                else:
                    logging.warning(
                        f"{self.thread_id}: No values found to push for Kafka {self.message_type}"
                    )
            except Exception as e:
                logging.error(
                    f"{self.thread_id}: The metric publish to BigQuery failed for {self.message_type.upper()}: {e}"
                )
                return
        elif os.getenv("DESTINATION_DB") == "MONGODB":
            time = parser.parse(period)
            count_list = []
            for road, rsu_counts in current_counts.items():
                for ip, count in rsu_counts.items():
                    document = {
                        "ip": ip,
                        "road": road,
                        "timestamp": time,
                        "message_type": self.message_type.upper(),
                        "count": count,
                    }
                    logging.debug(document)
                    count_list.append(document)

            logging.info(f"Mongo db publishing messages : {str(count_list)}")
            try:
                if len(count_list) > 0:
                    self.write_mongo(count_list)
                else:
                    logging.warning(
                        f"{self.thread_id}: No values found to push for Kafka {self.message_type}"
                    )
            except Exception as e:
                logging.error(
                    f"{self.thread_id}: The metric publish to MongoDB failed for {self.message_type.upper()}: {e}"
                )
                return

        logging.info(f"{self.thread_id}: Metrics published")

    # Called for every message that the subscription receives
    def process_message(self, message):
        try:
            # Check if malformed message
            jsonmsg = json.loads(message.value().decode("utf8"))

            if self.type == 0:
                contentKey = self.message_type.capitalize() + "MessageContent"
                item_arr = jsonmsg[contentKey]

                for msg_content in item_arr:
                    if "originRsu" in msg_content["metadata"]:
                        originIp = str(msg_content["metadata"]["originRsu"])
                    else:
                        logging.warning(
                            f"{self.thread_id}: Malformed message detected. No source IP."
                        )
                        originIp = "noIP"

                    if originIp in self.rsu_location_dict:
                        self.rsu_count_dict[self.rsu_location_dict[originIp]][
                            originIp
                        ] += 1
                    else:
                        self.rsu_location_dict[originIp] = "Unknown"
                        self.rsu_count_dict["Unknown"][originIp] = 1
            else:
                if "originIp" in jsonmsg["metadata"]:
                    originIp = str(jsonmsg["metadata"]["originIp"])
                else:
                    logging.warning(
                        f"{self.thread_id}: Malformed message detected. No source IP."
                    )
                    originIp = "noIP"

                if originIp in self.rsu_location_dict:
                    self.rsu_count_dict[self.rsu_location_dict[originIp]][originIp] += 1
                else:
                    self.rsu_location_dict[originIp] = "Unknown"
                    self.rsu_count_dict["Unknown"][originIp] = 1
        except Exception as e:
            logging.error(
                f"{self.thread_id}: A Kafka message failed to be processed with the following error: {e}"
            )

    def should_run(self):
        return True

    def listen_for_message_and_process(self, topic, bootstrap_server):
        logging.debug(
            f"{self.thread_id}: Listening for messages on Kafka topic {topic}..."
        )

        if os.getenv("KAFKA_TYPE", "") == "CONFLUENT":
            username = os.getenv("CONFLUENT_KEY")
            password = os.getenv("CONFLUENT_SECRET")
            conf = {
                "bootstrap.servers": bootstrap_server,
                "security.protocol": "SASL_SSL",
                "sasl.mechanism": "PLAIN",
                "sasl.username": username,
                "sasl.password": password,
                "group.id": f"{self.thread_id}-counter",
                "auto.offset.reset": "latest",
            }
        else:
            conf = {
                "bootstrap.servers": bootstrap_server,
                "group.id": f"{self.thread_id}-counter",
                "auto.offset.reset": "latest",
            }

        consumer = Consumer(conf)
        try:
            consumer.subscribe([topic])

            while self.should_run():
                msg = consumer.poll(timeout=1.0)
                if msg is None:
                    continue

                if msg.error():
                    if msg.error().code() == KafkaError._PARTITION_EOF:
                        # End of partition event
                        logging.warning(
                            "Topic %s [%d] reached end at offset %d\n"
                            % (msg.topic(), msg.partition(), msg.offset())
                        )
                    elif msg.error():
                        raise KafkaException(msg.error())
                else:
                    self.process_message(msg)
        finally:
            # Close down consumer to commit final offsets.
            consumer.close()
            logging.warning(
                f"{self.thread_id}: Disconnected from Kafka topic, reconnecting..."
            )

    def get_topic_from_type(self):
        # 0 - in metric
        # 1 - out metric
        if self.type == 0:
            topic = f"topic.OdeRawEncoded{self.message_type.upper()}Json"
        else:
            topic = f"topic.Ode{self.message_type.capitalize()}Json"
        return topic

    # Read from Kafka topic indefinitely
    def read_topic(self):
        topic = self.get_topic_from_type()
        bootstrap_server = os.getenv("ODE_KAFKA_BROKERS")

        self.listen_for_message_and_process(topic, bootstrap_server)

    def start_counter(self):
        # Setup scheduler for async metric uploads
        scheduler = BackgroundScheduler({"apscheduler.timezone": "UTC"})
        scheduler.add_job(self.push_metrics, "cron", minute="0")
        scheduler.start()

        logging.info(
            f"{self.thread_id}: Starting up {self.message_type.upper()} Kafka Metric thread..."
        )
        self.read_topic()
