from unittest.mock import MagicMock, patch, Mock
from common import pgquery
import sqlalchemy
import os

# test that init_tcp_connection_engine is calling sqlalchemy.create_engine with expected arguments
@patch('common.pgquery.db_config', new={'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800})
def test_init_tcp_connection_engine():
    # mock return values for function dependencies
    sqlalchemy.create_engine = MagicMock(
        return_value = "myengine"
    )
    sqlalchemy.engine.url.URL.create = MagicMock(
        return_value = "myurl"
    )

    # call function
    db_user = "user"
    db_pass = "pass"
    db_name = "mydatabase"
    db_hostname = "myhostname"
    db_port = 3000
    engine_pool = pgquery.init_tcp_connection_engine(db_user, db_pass, db_name, db_hostname, db_port)
    
    # check return value
    assert(engine_pool == "myengine")

    # check that sqlalchemy.engine.url.URL.create was called with expected arguments
    sqlalchemy.engine.url.URL.create.assert_called_once_with(
        drivername='postgresql+pg8000',
        username=db_user,
        password=db_pass,
        host=db_hostname,
        port=db_port,
        database=db_name
        )
    
    # check that sqlalchemy.create_engine was called with expected arguments
    my_db_config = {'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800}
    sqlalchemy.create_engine.assert_called_once_with("myurl", **my_db_config)

# test that init_socket_connection_engine is calling sqlalchemy.create_engine with expected arguments
@patch('common.pgquery.db_config', new={'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800})
def test_init_socket_connection_engine():
    # mock return values for function dependencies
    sqlalchemy.create_engine = MagicMock(
        return_value = "myengine"
    )
    sqlalchemy.engine.url.URL.create = MagicMock(
        return_value = "myurl"
    )

    # call function
    db_user = "user"
    db_pass = "pass"
    db_name = "mydatabase"
    unix_query = {'unix_sock': '/cloudsql/myproject:us-central1:myinstance'}
    engine_pool = pgquery.init_socket_connection_engine(db_user, db_pass, db_name, unix_query)

    # check return value
    assert(engine_pool == "myengine")

    # check that sqlalchemy.engine.url.URL.create was called with expected arguments
    sqlalchemy.engine.url.URL.create.assert_called_once_with(
        drivername='postgresql+pg8000',
        username=db_user,
        password=db_pass,
        database=db_name,
        query=unix_query
    )

    # check that sqlalchemy.create_engine was called with expected arguments
    my_db_config = {'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800}
    sqlalchemy.create_engine.assert_called_once_with("myurl", **my_db_config)

# test initializing tcp connection engine based on environment variables
@patch('common.pgquery.db_config', new={'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800})
def test_init_connection_engine_target_tcp():
    # mock return values for function dependencies
    pgquery.init_tcp_connection_engine = MagicMock(
        return_value = "myengine1"
    )
    pgquery.init_socket_connection_engine = MagicMock(
        return_value = "myengine2"
    )

    db_user = "user"
    db_pass = "pass"
    db_name = "mydatabase"
    db_hostname = "myhostname:3000"

    # set environment variables
    os.environ['DB_USER'] = db_user
    os.environ['DB_PASS'] = db_pass
    os.environ['DB_NAME'] = db_name
    os.environ["DB_HOST"] = db_hostname

    host_args = db_hostname.split(":")
    db_hostname, db_port = host_args[0], int(host_args[1])

    # call function
    engine_pool = pgquery.init_connection_engine()

    # check return value
    assert(engine_pool == "myengine1")

    # check that init_tcp_connection_engine was called with expected arguments
    pgquery.init_tcp_connection_engine.assert_called_once_with(db_user, db_pass, db_name, db_hostname, db_port)
    
    # check that init_socket_connection_engine was not called
    pgquery.init_socket_connection_engine.assert_not_called()

# test initializing socket connection engine based on environment variables
@patch('common.pgquery.db_config', new={'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800})
@patch('common.pgquery.db', new=None)
def test_init_connection_engine_target_socket():
    # mock return values for function dependencies
    pgquery.init_tcp_connection_engine = MagicMock(
        return_value = "myengine1"
    )
    pgquery.init_socket_connection_engine = MagicMock(
        return_value = "myengine2"
    )

    db_user = "user"
    db_pass = "pass"
    db_name = "mydatabase"

    # set environment variables
    os.environ['DB_USER'] = db_user
    os.environ['DB_PASS'] = db_pass
    os.environ['DB_NAME'] = db_name
    os.environ['INSTANCE_CONNECTION_NAME'] = "myproject:us-central1:myinstance"

    unix_query = {
        "unix_sock": f"/cloudsql/{os.environ['INSTANCE_CONNECTION_NAME']}/.s.PGSQL.5432"
    }

    # call function
    engine_pool = pgquery.init_connection_engine()

    # check return value
    assert(engine_pool == "myengine2")

    # check that init_socket_connection_engine was called with expected arguments
    pgquery.init_socket_connection_engine.assert_called_once_with(db_user, db_pass, db_name, unix_query)
    
    # check that init_tcp_connection_engine was not called
    pgquery.init_tcp_connection_engine.assert_not_called()

# test that query_db is calling engine.connect and connection.execute with expected arguments
@patch('common.pgquery.db_config', new={'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800})
@patch('common.pgquery.db', new=None)
def test_query_db():
    pgquery.init_connection_engine = MagicMock(
        return_value = Mock( # return a mock engine
            connect = MagicMock(
                return_value = Mock( # return a mock connection iterator
                    __enter__ = MagicMock(
                        return_value = Mock( # return a mock connection
                            execute = MagicMock(
                                return_value = Mock( # return a mock result
                                    fetchall = MagicMock(
                                        return_value = "myresult"
                                    )
                                )
                            )
                        )
                    ),
                    __exit__ = MagicMock()
                )
            )
        )
    )

    # call function
    query = "SELECT * FROM mytable"
    result = pgquery.query_db(query)

    # check return value
    assert(result == "myresult")

    # check that init_connection_engine was called once
    pgquery.init_connection_engine.assert_called_once()

# test that write_db is calling engine.connect and connection.execute with expected arguments
@patch('common.pgquery.db_config', new={'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800})
@patch('common.pgquery.db', new=None)
def test_write_db():
    pgquery.init_connection_engine = MagicMock(
        return_value = Mock( # return a mock engine
            connect = MagicMock(
                return_value = Mock( # return a mock connection iterator
                    __enter__ = MagicMock(
                        return_value = Mock( # return a mock connection
                            execute = MagicMock(
                                return_value = Mock( # return a mock result
                                    fetchall = MagicMock(
                                        return_value = "myresult"
                                    )
                                )
                            )
                        )
                    ),
                    __exit__ = MagicMock()
                )
            )
        )
    )

    # call function
    query = "INSERT INTO table (column) VALUES ('value')"
    pgquery.write_db(query)

    # check that init_connection_engine was called once
    pgquery.init_connection_engine.assert_called_once()