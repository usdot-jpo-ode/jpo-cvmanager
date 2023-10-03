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
  "pool_recycle": 60,  # 1 minutes
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
      database=db_name,  # e.g. "my-database-name"
    ),
    **db_config,
  )

  pool.dialect.description_encoding = None
  logging.info("DB pool created!")
  return pool

def query_db(query_string, no_return = False):
  global db
  if db is None:
    db = init_tcp_connection_engine()

  logging.debug("DB connection starting...")
  with db.connect() as conn:
    # Execute the query and fetch all results    
    logging.debug(f'Executing query "{query_string};"...')
    if no_return:
      conn.execute(sqlalchemy.text(query_string))
      conn.commit()
    else:
      result = conn.execute(sqlalchemy.text(query_string)).fetchall()
      return result
