import os
import sqlalchemy
import logging

db_config = {
  # Pool size is the maximum number of permanent connections to keep.
  "pool_size": 5,
  # Temporarily exceeds the set pool_size if no connections are available.
  "max_overflow": 2,
  # Maximum number of seconds to wait when retrieving a
  # new connection from the pool. After the specified amount of time, an
  # exception will be thrown.
  "pool_timeout": 30,  # 30 seconds
  # 'pool_recycle' is the maximum number of seconds a connection can persist.
  # Connections that live longer than the specified amount of time will be
  # reestablished
  "pool_recycle": 60  # 1 minutes
}

db = None

def init_tcp_connection_engine():
  db_user = os.environ["DB_USER"]
  db_pass = os.environ["DB_PASS"]
  db_name = os.environ["DB_NAME"]
  db_host = os.environ["DB_HOST"]

  # Extract host and port from db_host
  host_args = db_host.split(":")
  db_hostname, db_port = host_args[0], int(host_args[1])

  logging.info(f"Creating DB pool to {db_hostname}:{db_port}")
  pool = sqlalchemy.create_engine(
    # Equivalent URL:
    # postgresql+pg8000://<db_user>:<db_pass>@<db_host>:<db_port>/<db_name>
    sqlalchemy.engine.url.URL.create(
      drivername="postgresql+pg8000",
      username=db_user,  # e.g. "my-database-user"
      password=db_pass,  # e.g. "my-database-password"
      host=db_hostname,  # e.g. "127.0.0.1"
      port=db_port,  # e.g. 5432
      database=db_name  # e.g. "my-database-name"
    ),
    **db_config
  )

  pool.dialect.description_encoding = None
  logging.info("DB pool created!")
  return pool

def get_rsu_data():
  global db
  if db is None:
    db = init_tcp_connection_engine()

  result = []

  with db.connect() as conn:
    # Execute the query and fetch all results
    query = "SELECT rsu_id, ipv4_address FROM public.rsus ORDER BY rsu_id"

    logging.debug(f'Executing query "{query};"...')
    data = conn.execute(query).fetchall()

    logging.debug('Parsing results...')
    for point in data:
      rsu = {
        'rsu_id': point[0],
        'rsu_ip': str(point[1])
      }
      result.append(rsu)

  return result

def insert_rsu_ping(request_json):
  global db
  if db is None:
    db = init_tcp_connection_engine()

  with db.connect() as conn:
    rsu_id = request_json["rsu_id"]
    histories = request_json["histories"]
    logging.debug(
      f'Inserting {len(histories)} new Ping records for RsuData {rsu_id}')
    for history in histories:
      try:
        query = f'INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp({history["clock"]}), B\'{history["value"]}\', {rsu_id})'
        conn.execute(query)
      except Exception as e:
        logging.exception(f"Error inserting Ping record: {e}")
        return False
  return True
