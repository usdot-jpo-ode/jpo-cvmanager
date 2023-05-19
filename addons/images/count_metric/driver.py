from google.oauth2 import id_token
import google.auth.transport.requests
import os
import copy
import threading
import requests
import logging

from kafka_counter import KafkaMessageCounter

# Set based on project and subscription, set these outside of the script if deployed
# os.environ['MESSAGE_TYPES'] = 'bsm'
# os.environ['PROJECT_ID'] = 'cdot-oim-cv-dev'
# os.environ['ODE_KAFKA_BROKERS'] = 'localhost:9092'
# os.environ['KAFKA_BIGQUERY_TABLENAME'] = 'cdot-oim-cv-dev.RsuManagerDataset.kafka-rsucounts-test'
# os.environ['PUBSUB_BIGQUERY_TABLENAME'] = 'cdot-oim-cv-dev.RsuManagerDataset.rsucounts-test'
# os.environ['RSU_INFO_ENDPOINT'] = 'us-central1-cdot-oim-cv-dev.cloudfunctions.net/rsu-info'

thread_pool = []

rsu_location_dict = {}
rsu_count_dict = {}

def gen_auth(endpoint):
  logging.debug(f"Generating auth token for: {endpoint}")
  auth_req = google.auth.transport.requests.Request()
  token = id_token.fetch_id_token(auth_req, endpoint)
  return token

# Create template dictionaries for RSU roads and counts using HTTP JSON data
def populateRsuDict(rsujson):
  for rsu in rsujson['rsuList']:
    rsuip = rsu['properties']['ipv4_address']
    proute = rsu['properties']['primary_route']

    rsu_location_dict[rsuip] = proute
    # Add IP to dict if the road exists in the dict already
    if proute in rsu_count_dict:
      rsu_count_dict[proute][rsuip] = 0
    else:
      rsu_count_dict[proute] = {rsuip: 0}

  rsu_count_dict['Unknown'] = {}

def run():
  # Pull list of message types to run counts for from environment variable
  messageTypesString = os.getenv('MESSAGE_TYPES')
  if messageTypesString is None:
    logging.error("MESSAGE_TYPES environment variable not set! Exiting.")
    exit("MESSAGE_TYPES environment variable not set! Exiting.")
  message_types = [msgtype.strip().lower() for msgtype in messageTypesString.split(',')]

  # Configure logging based on ENV var or use default if not set
  log_level = 'INFO' if "LOGGING_LEVEL" not in os.environ else os.environ['LOGGING_LEVEL']
  logging.basicConfig(format='%(levelname)s:%(message)s', level=log_level)

  endpoint = os.getenv('RSU_INFO_ENDPOINT')
  if endpoint is None:
    logging.error("RSU_INFO_ENDPOINT environment variable not set! Exiting.")
    exit("RSU_INFO_ENDPOINT environment variable not set! Exiting.")
  try:
    token = gen_auth(endpoint)
    h = {
      "Content-Type": "application/json",
      "Authorization": f"Bearer {token}"
    }
    logging.info(f"Requesting RSU info JSON from {endpoint}")
    response = requests.get(endpoint, headers=h)
    if response.status_code != 200:
      logging.error(f'RSU info could not be obtained: {response.text}')
      exit()
  except Exception as e:
    logging.error(f'RSU info could not be obtained: {e}')
    exit()
  
  logging.debug(f"Response JSON received: {response.json()}")
  logging.debug("Creating RSU and count dictionaries...")
  populateRsuDict(response.json())

  logging.info("Creating Data-In Kafka count threads...")
  # Start the Kafka counters on their own threads
  for message_type in message_types:
    counter = KafkaMessageCounter(f'KAFKA_IN_{message_type.upper()}', message_type, copy.deepcopy(rsu_location_dict), copy.deepcopy(rsu_count_dict), copy.deepcopy(rsu_count_dict), 0)
    new_thread = threading.Thread(target=counter.start_counter)
    new_thread.start()
    thread_pool.append(new_thread)

  logging.info("Creating Data-Out Kafka count threads...")
  # Start the Kafka counters on their own threads
  for message_type in message_types:
    counter = KafkaMessageCounter(f'KAFKA_OUT_{message_type.upper()}', message_type, copy.deepcopy(rsu_location_dict), copy.deepcopy(rsu_count_dict), copy.deepcopy(rsu_count_dict), 1)
    new_thread = threading.Thread(target=counter.start_counter)
    new_thread.start()
    thread_pool.append(new_thread)

  for thread in thread_pool:
    thread.join()
    logging.debug("Closed thread")
    
if __name__ == '__main__': 
  run()