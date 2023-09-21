from unittest.mock import MagicMock, patch, Mock
from src import pgquery
import sqlalchemy
import os


# test that init_tcp_connection_engine is calling sqlalchemy.create_engine with expected arguments
@patch("src.pgquery.db_config", new={"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800})
def test_init_tcp_connection_engine():
    # mock return values for function dependencies
    sqlalchemy.create_engine = MagicMock(return_value="myengine")
    sqlalchemy.engine.url.URL.create = MagicMock(return_value="myurl")

    # call function
    PG_DB_USER = "user"
    PG_DB_PASS = "pass"
    PG_DB_NAME = "mydatabase"
    PG_DB_IP = "8.8.8.8"
    PG_DB_PORT = 3000
    engine_pool = pgquery.init_tcp_connection_engine(PG_DB_USER, PG_DB_PASS, PG_DB_NAME, PG_DB_IP, PG_DB_PORT)

    # check return value
    assert engine_pool == "myengine"

    # check that sqlalchemy.engine.url.URL.create was called with expected arguments
    sqlalchemy.engine.url.URL.create.assert_called_once_with(
        drivername="postgresql+pg8000",
        username=PG_DB_USER,
        password=PG_DB_PASS,
        host=PG_DB_IP,
        port=PG_DB_PORT,
        database=PG_DB_NAME,
    )

    # check that sqlalchemy.create_engine was called with expected arguments
    my_db_config = {"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800}
    sqlalchemy.create_engine.assert_called_once_with("myurl", **my_db_config)


# test initializing tcp connection engine based on environment variables
@patch("src.pgquery.db_config", new={"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800})
def test_init_connection_engine_target_tcp():
    sqlalchemy.create_engine = MagicMock(return_value="myengine2")
    sqlalchemy.engine.url.URL.create = MagicMock(return_value="myurl")

    # call function
    PG_DB_USER = "user"
    PG_DB_PASS = "pass"
    PG_DB_NAME = "mydatabase"
    PG_DB_IP = "8.8.8.8"
    PG_DB_PORT = 3000
    engine_pool = pgquery.init_tcp_connection_engine(PG_DB_USER, PG_DB_PASS, PG_DB_NAME, PG_DB_IP, PG_DB_PORT)

    # check return value
    assert engine_pool == "myengine2"

    # check that sqlalchemy.engine.url.URL.create was called with expected arguments
    sqlalchemy.engine.url.URL.create.assert_called_once_with(
        drivername="postgresql+pg8000",
        username=PG_DB_USER,
        password=PG_DB_PASS,
        host=PG_DB_IP,
        port=PG_DB_PORT,
        database=PG_DB_NAME,
    )

    # check that sqlalchemy.create_engine was called with expected arguments
    my_db_config = {"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800}
    sqlalchemy.create_engine.assert_called_once_with("myurl", **my_db_config)


# test that query_db is calling engine.connect and connection.execute with expected arguments
@patch("src.pgquery.db_config", new={"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800})
@patch("src.pgquery.db", new=None)
def test_query_db():
    pgquery.init_connection_engine = MagicMock(
        return_value=Mock(  # return a mock engine
            connect=MagicMock(
                return_value=Mock(  # return a mock connection iterator
                    __enter__=MagicMock(
                        return_value=Mock(  # return a mock connection
                            execute=MagicMock(
                                return_value=Mock(fetchall=MagicMock(return_value="myresult"))  # return a mock result
                            )
                        )
                    ),
                    __exit__=MagicMock(),
                )
            )
        )
    )

    # call function
    query = "SELECT * FROM mytable"
    result = pgquery.query_db(query)

    # check return value
    assert result == "myresult"

    # check that init_connection_engine was called once
    pgquery.init_connection_engine.assert_called_once()


# test that insert_db is calling engine.connect and connection.execute with expected arguments
@patch("src.pgquery.db_config", new={"pool_size": 5, "max_overflow": 2, "pool_timeout": 30, "pool_recycle": 1800})
@patch("src.pgquery.db", new=None)
def test_insert_db():
    pgquery.init_connection_engine = MagicMock(
        return_value=Mock(  # return a mock engine
            connect=MagicMock(
                return_value=Mock(  # return a mock connection iterator
                    __enter__=MagicMock(
                        return_value=Mock(  # return a mock connection
                            execute=MagicMock(
                                return_value=Mock(fetchall=MagicMock(return_value="myresult"))  # return a mock result
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
    pgquery.insert_db(query)

    # check that init_connection_engine was called once
    pgquery.init_connection_engine.assert_called_once()
