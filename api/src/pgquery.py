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


def init_tcp_connection_engine(PG_DB_USER, PG_DB_PASS, PG_DB_NAME, PG_DB_IP, PG_DB_PORT):
    logging.info(f"Creating DB pool")
    pool = sqlalchemy.create_engine(
        # Equivalent URL:
        # postgresql+pg8000://<PG_DB_USER>:<PG_DB_PASS>@<PG_DB_IP>:<db_port>/<db_name>
        sqlalchemy.engine.url.URL.create(
            drivername="postgresql+pg8000",
            username=PG_DB_USER,  # e.g. "my-database-user"
            password=PG_DB_PASS,  # e.g. "my-database-password"
            host=PG_DB_IP,  # e.g. "127.0.0.1"
            port=PG_DB_PORT,  # e.g. 5432
            database=PG_DB_NAME,  # e.g. "my-database-name"
        ),
        **db_config,
    )
    # pool.dialect.description_encoding = None
    logging.info("DB pool created!")
    return pool


def init_connection_engine():
    PG_DB_USER = os.environ["PG_DB_USER"]
    PG_DB_PASS = os.environ["PG_DB_PASS"]
    PG_DB_NAME = os.environ["PG_DB_NAME"]
    PG_DB_IP = os.environ["PG_DB_IP"]
    PG_DB_PORT = os.environ["PG_DB_PORT"]
    return init_tcp_connection_engine(PG_DB_USER, PG_DB_PASS, PG_DB_NAME, PG_DB_IP, PG_DB_PORT)


def query_db(query):
    global db
    if db is None:
        db = init_connection_engine()

    logging.info("DB connection starting...")
    with db.connect() as conn:
        logging.debug("Executing query...")
        data = conn.execute(query).fetchall()
        return data


def insert_db(query):
    global db
    if db is None:
        db = init_connection_engine()

    logging.info("DB connection starting...")
    with db.connect() as conn:
        logging.debug("Executing insert query...")
        conn.execute(query)
