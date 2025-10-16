from unittest.mock import MagicMock, patch, Mock
from common import pgquery
import sqlalchemy
import os


# test that init_tcp_connection_engine is calling sqlalchemy.create_engine with expected arguments
@patch(
    "common.pgquery.db_config",
    new={"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800},
)
def test_init_tcp_connection_engine():
    # mock return values for function dependencies
    sqlalchemy.create_engine = MagicMock(return_value="my_engine")
    sqlalchemy.engine.url.URL.create = MagicMock(return_value="my_url")

    # call function
    db_user = "user"
    db_pass = "pass"
    db_name = "my_database"
    db_hostname = "my_hostname"
    db_port = 3000
    engine_pool = pgquery.init_tcp_connection_engine(
        db_user, db_pass, db_name, db_hostname, db_port
    )

    # check return value
    assert engine_pool == "my_engine"

    # check that sqlalchemy.engine.url.URL.create was called with expected arguments
    sqlalchemy.engine.url.URL.create.assert_called_once_with(
        drivername="postgresql+pg8000",
        username=db_user,
        password=db_pass,
        host=db_hostname,
        port=db_port,
        database=db_name,
    )

    # check that sqlalchemy.create_engine was called with expected arguments
    my_db_config = {
        "pool_size": 5,
        "max_overflow": 2,
        "pool_timeout": 30,
        "pool_recycle": 1800,
    }
    sqlalchemy.create_engine.assert_called_once_with("my_url", **my_db_config)


# test that init_socket_connection_engine is calling sqlalchemy.create_engine with expected arguments
@patch(
    "common.pgquery.db_config",
    new={"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800},
)
def test_init_socket_connection_engine():
    # mock return values for function dependencies
    sqlalchemy.create_engine = MagicMock(return_value="my_engine")
    sqlalchemy.engine.url.URL.create = MagicMock(return_value="my_url")

    # call function
    db_user = "user"
    db_pass = "pass"
    db_name = "my_database"
    unix_query = {"unix_sock": "/cloudsql/my_project:us-central1:my_instance"}
    engine_pool = pgquery.init_socket_connection_engine(
        db_user, db_pass, db_name, unix_query
    )

    # check return value
    assert engine_pool == "my_engine"

    # check that sqlalchemy_.engine.url.URL.create was called with expected arguments
    sqlalchemy.engine.url.URL.create.assert_called_once_with(
        drivername="postgresql+pg8000",
        username=db_user,
        password=db_pass,
        database=db_name,
        query=unix_query,
    )

    # check that sqlalchemy.create_engine was called with expected arguments
    my_db_config = {
        "pool_size": 5,
        "max_overflow": 2,
        "pool_timeout": 30,
        "pool_recycle": 1800,
    }
    sqlalchemy.create_engine.assert_called_once_with("my_url", **my_db_config)


# test initializing tcp connection engine based on environment variables
@patch(
    "common.pgquery.db_config",
    new={"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800},
)
def test_init_connection_engine_target_tcp():
    # mock return values for function dependencies
    pgquery.init_tcp_connection_engine = MagicMock(return_value="my_engine1")
    pgquery.init_socket_connection_engine = MagicMock(return_value="my_engine2")

    db_user = "user"
    db_pass = "pass"
    db_name = "my_database"
    db_hostname = "my_hostname:3000"

    # set environment variables
    os.environ["PG_DB_USER"] = db_user
    os.environ["PG_DB_PASS"] = db_pass
    os.environ["PG_DB_NAME"] = db_name
    os.environ["PG_DB_HOST"] = db_hostname

    host_args = db_hostname.split(":")
    db_hostname, db_port = host_args[0], int(host_args[1])

    # call function
    engine_pool = pgquery.init_connection_engine()

    # check return value
    assert engine_pool == "my_engine1"

    # check that init_tcp_connection_engine was called with expected arguments
    pgquery.init_tcp_connection_engine.assert_called_once_with(
        db_user, db_pass, db_name, db_hostname, db_port
    )

    # check that init_socket_connection_engine was not called
    pgquery.init_socket_connection_engine.assert_not_called()


# test initializing socket connection engine based on environment variables
@patch(
    "common.pgquery.db_config",
    new={"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800},
)
@patch("common.pgquery.db", new=None)
def test_init_connection_engine_target_socket():
    # mock return values for function dependencies
    pgquery.init_tcp_connection_engine = MagicMock(return_value="my_engine1")
    pgquery.init_socket_connection_engine = MagicMock(return_value="my_engine2")

    db_user = "user"
    db_pass = "pass"
    db_name = "my_database"

    # set environment variables
    os.environ["PG_DB_USER"] = db_user
    os.environ["PG_DB_PASS"] = db_pass
    os.environ["PG_DB_NAME"] = db_name
    os.environ["INSTANCE_CONNECTION_NAME"] = "my_project:us-central1:my_instance"

    unix_query = {
        "unix_sock": f"/cloudsql/{os.environ['INSTANCE_CONNECTION_NAME']}/.s.PGSQL.5432"
    }

    # call function
    engine_pool = pgquery.init_connection_engine()

    # check return value
    assert engine_pool == "my_engine2"

    # check that init_socket_connection_engine was called with expected arguments
    pgquery.init_socket_connection_engine.assert_called_once_with(
        db_user, db_pass, db_name, unix_query
    )

    # check that init_tcp_connection_engine was not called
    pgquery.init_tcp_connection_engine.assert_not_called()


# test that query_db is calling engine.connect and connection.execute with expected arguments
@patch(
    "common.pgquery.db_config",
    new={"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800},
)
@patch("common.pgquery.db", new=None)
def test_query_db():
    pgquery.init_connection_engine = MagicMock(
        return_value=Mock(  # return a mock engine
            connect=MagicMock(
                return_value=Mock(  # return a mock connection iterator
                    __enter__=MagicMock(
                        return_value=Mock(  # return a mock connection
                            execute=MagicMock(
                                return_value=Mock(  # return a mock result
                                    fetchall=MagicMock(return_value="my_result")
                                )
                            )
                        )
                    ),
                    __exit__=MagicMock(),
                )
            )
        )
    )

    # call function
    query = "SELECT * FROM my_table"
    result = pgquery.query_db(query)

    # check return value
    assert result == "my_result"

    # check that init_connection_engine was called once
    pgquery.init_connection_engine.assert_called_once()


# test that write_db is calling engine.connect and connection.execute with expected arguments
@patch(
    "common.pgquery.db_config",
    new={"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800},
)
@patch("common.pgquery.db", new=None)
def test_write_db():
    pgquery.init_connection_engine = MagicMock(
        return_value=Mock(  # return a mock engine
            connect=MagicMock(
                return_value=Mock(  # return a mock connection iterator
                    __enter__=MagicMock(
                        return_value=Mock(  # return a mock connection
                            execute=MagicMock(
                                return_value=Mock(  # return a mock result
                                    fetchall=MagicMock(return_value="my_result")
                                )
                            )
                        )
                    ),
                    __exit__=MagicMock(),
                )
            )
        )
    )

    # call function
    query = "INSERT INTO table (column) VALUES ('value')"
    pgquery.write_db(query)

    # check that init_connection_engine was called once
    pgquery.init_connection_engine.assert_called_once()


@patch("common.pgquery.query_db")
def test_query_and_return_list(mock_query_db):
    # sqlalchemy returns a list of tuples. This test replicates the tuple list
    mock_query_db.return_value = [
        (
            "AAA",
            "BBB",
        ),
        ("CCC",),
    ]
    expected_data = ["AAA BBB", "CCC"]
    expected_query = "SELECT * FROM test"
    actual_result = pgquery.query_and_return_list("SELECT * FROM test")

    mock_query_db.assert_called_with(expected_query)
    assert actual_result == expected_data
