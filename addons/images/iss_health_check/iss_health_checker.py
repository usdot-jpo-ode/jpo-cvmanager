from datetime import datetime
import requests
import logging
import os
import iss_token
import pgquery

def get_rsu_data():
  result = {}
  query = "SELECT jsonb_build_object('rsu_id', rsu_id, 'iss_scms_id', iss_scms_id) " \
          "FROM public.rsus " \
          "WHERE iss_scms_id IS NOT NULL " \
          "ORDER BY rsu_id"
  data = pgquery.query_db(query)

  logging.debug('Parsing results...')
  for point in data:
    point_dict = dict(point[0])
    result[point_dict['iss_scms_id']] = {
      'rsu_id': point_dict['rsu_id']
    }

  return result

def get_scms_status_data():
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
    iss_request = iss_base + "?pageSize={}&page={}&project_id={}".format(page_size, page, project_id)
    logging.debug("GET: " + iss_request)
    response = requests.get(iss_request, headers=iss_headers)
    enrollment_list = response.json()["data"]

    if len(enrollment_list) == 0:
      break

    # Loop through each device on current page
    for enrollment_status in enrollment_list:
      if enrollment_status["_id"] in rsu_data:
        rsu_data[enrollment_status["_id"]]['provisionerCompany'] = enrollment_status["provisionerCompany_id"]
        rsu_data[enrollment_status["_id"]]['entityType'] = enrollment_status["entityType"]
        rsu_data[enrollment_status["_id"]]['project_id'] = enrollment_status["project_id"]
        rsu_data[enrollment_status["_id"]]['deviceHealth'] = enrollment_status["deviceHealth"]

        # If the device has yet to download its first set of certs, set the expiration time to when it was enrolled
        if "authorizationCertInfo" in enrollment_status["enrollments"][0]:
          rsu_data[enrollment_status["_id"]]['expiration'] = enrollment_status["enrollments"][0]["authorizationCertInfo"]["expireTimeOfLatestDownloadedCert"]
        else:
          rsu_data[enrollment_status["_id"]]['expiration'] = None

      messages_processed = messages_processed + 1

    page = page + 1

  logging.info("Processed {} messages".format(messages_processed))
  return rsu_data

def insert_scms_data(data):
  logging.info('Inserting SCMS data into PostgreSQL...')
  now_ts = datetime.strftime(datetime.now(), '%Y-%m-%dT%H:%M:%S.000Z')

  query = "INSERT INTO public.scms_health(\"timestamp\", health, expiration, rsu_id) VALUES"
  for value in data.values():
    health = '1' if value['deviceHealth'] == 'Healthy' else '0'
    if value['expiration']:
      query = query + \
            f" ('{now_ts}', '{health}', '{value['expiration']}', {value['rsu_id']}),"
    else:
      query = query + \
            f" ('{now_ts}', '{health}', NULL, {value['rsu_id']}),"

  pgquery.query_db(query[:-1], no_return = True)
  logging.info('SCMS data inserted {} messages into PostgreSQL...'.format(len(data.values())))

if __name__ == "__main__":
  # Configure logging based on ENV var or use default if not set
  log_level = 'INFO' if "LOGGING_LEVEL" not in os.environ else os.environ['LOGGING_LEVEL'] 
  logging.basicConfig(format='%(levelname)s:%(message)s', level=log_level)

  scms_statuses = get_scms_status_data()
  insert_scms_data(scms_statuses)