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


def init_tcp_connection_engine(db_user, db_pass, db_name, db_hostname, db_port):
    logging.info("Creating DB pool")
    logging.debug(f"{db_user},{db_name},{db_hostname},{db_port}")
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
    # pool.dialect.description_encoding = None
    logging.info("DB pool created!")
    return pool


def init_socket_connection_engine(db_user, db_pass, db_name, unix_query):
    logging.info("Creating DB pool")
    pool = sqlalchemy.create_engine(
        # Equivalent URL:
        # postgresql+pg8000://<db_user>:<db_pass>@/<db_name>?unix_sock=/cloudsql/<cloud_sql_instance_name>
        sqlalchemy.engine.url.URL.create(
            drivername="postgresql+pg8000",
            username=db_user,  # e.g. "my-database-user"
            password=db_pass,  # e.g. "my-database-password"
            database=db_name,  # e.g. "my-database-name"
            query=unix_query,
        ),
        **db_config,
    )
    logging.info("DB pool created!")
    return pool


def init_connection_engine():
    db_user = os.environ["PG_DB_USER"]
    db_pass = os.environ["PG_DB_PASS"]
    db_name = os.environ["PG_DB_NAME"]
    if (
        "INSTANCE_CONNECTION_NAME" in os.environ
        and os.environ["INSTANCE_CONNECTION_NAME"].strip()
    ):
        logging.debug("Using socket connection")
        instance_connection_name = os.environ["INSTANCE_CONNECTION_NAME"]
        unix_query = {
            "unix_sock": f"/cloudsql/{instance_connection_name}/.s.PGSQL.5432"
        }
        return init_socket_connection_engine(db_user, db_pass, db_name, unix_query)
    else:
        logging.debug("Using tcp connection")
        db_host = os.environ["PG_DB_HOST"]
        # Extract host and port from db_host
        host_args = db_host.split(":")
        db_hostname, db_port = host_args[0], int(host_args[1])
        return init_tcp_connection_engine(
            db_user, db_pass, db_name, db_hostname, db_port
        )


def query_db(query_string, params=None):
    """
    Execute a parameterized query against the database.

    Args:
        query_string (str): The SQL query string with placeholders for parameters.
                            Example: "SELECT * FROM table WHERE column = :value"
        params (dict, optional): A dictionary of parameters to bind to the query.
                                 Example: {"value": "some_value"}

    Returns:
        list: The result of the query as a list of rows.
    """
    global db
    if db is None:
        db = init_connection_engine()

    logging.info("DB connection starting...")
    with db.connect() as conn:
        logging.debug("Executing query...")
        data = conn.execute(sqlalchemy.text(query_string), params).fetchall()
        return data


def write_db(query_string, params=None):
    """
    Execute a parameterized write query (INSERT, UPDATE, DELETE) against the database.

    Args:
        query_string (str): The SQL query string with placeholders for parameters.
                            Example: "INSERT INTO table (column) VALUES (:value)"
        params (dict, optional): A dictionary of parameters to bind to the query.
                                 Example: {"value": "some_value"}

    Returns:
        None
    """
    global db
    if db is None:
        db = init_connection_engine()

    logging.info("DB connection starting...")
    with db.connect() as conn:
        logging.debug("Executing insert query...")
        conn.execute(sqlalchemy.text(query_string), params)
        conn.commit()


def write_db_batched(
    query_prefix: str,
    query_rows: list[tuple[str, dict]],
    query_suffix: str = "",
    base_params: dict = {},
    batch_size: int = 100,
):
    """
    Executes a series of similar write queries (such as batch inserts or updates) in batches,
    safely enumerating parameters to avoid SQL parameter name collisions and to stay within
    database parameter limits.

    Args:
        query_prefix (str): The SQL prefix for each query (e.g., "INSERT INTO ... VALUES").
        query_rows (list[tuple[str, dict]]): A list of tuples, each containing a query string
            (typically a VALUES clause or similar) and a dict of parameters for that row.
        query_suffix (str, optional): An optional SQL suffix to append after each batch (e.g., "RETURNING id").
        base_params (dict, optional): Parameters used by the query prefix or suffix.
        batch_size (int, optional): The maximum number of rows to include in each batch execution.

    Returns:
        None
    """
    # Each query stage comes with a query string, and a set of parameters
    for i in range(0, len(query_rows), batch_size):
        batch = query_rows[i : i + batch_size]
        query_strings = []
        all_params = base_params.copy()
        for query_string, params in batch:
            query_strings.append(query_string)
            all_params.update(params)
        full_query = query_prefix + ",".join(query_strings) + query_suffix
        write_db(full_query, params=all_params)


def query_and_return_list(query):
    data = query_db(query)
    return_list = []
    for row in data:
        return_list.append(" ".join(row))
    return return_list
