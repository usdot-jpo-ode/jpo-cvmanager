from unittest.mock import MagicMock, Mock, patch
import os

from images.iss_health_check import pgquery

@patch("images.iss_health_check.pgquery.sqlalchemy")
def test_init_tcp_connection_engine_counts(mock_sqlalchemy):
  # mock environment
  db_user = "db_user"
  db_pass = "db_pass"
  db_name = "db_name"
  db_host = "127.0.0.1:1234"

  os.environ["DB_USER"] = db_user
  os.environ["DB_PASS"] = db_pass
  os.environ["DB_NAME"] = db_name
  os.environ["DB_HOST"] = db_host

  # mock sqlalchemy
  mock_engine = MagicMock()
  mock_engine.dialect.description_encoding = None
  mock_sqlalchemy.create_engine.return_value = mock_engine

  # call
  result = pgquery.init_tcp_connection_engine()

  # check
  mock_sqlalchemy.create_engine.assert_called_once()
  mock_engine.dialect.description_encoding = None
  assert result == mock_engine

@patch('images.iss_health_check.pgquery.db', new=None)
@patch('images.iss_health_check.pgquery.init_tcp_connection_engine')
def test_query_db_return(mock_init_tcp_connection_engine):
  # mock
  mock_init_tcp_connection_engine.return_value = Mock( # returns db
    connect=MagicMock(
      return_value=Mock( # returns connection
        __enter__=MagicMock(
          return_value=Mock( # returns connection iterator
            execute=MagicMock(
              return_value=Mock( # returns data result
                fetchall=MagicMock(
                  return_value =  [
                      [{'rsu_id': 1, 'iss_scms_id': 'ABC'}],
                      [{'rsu_id': 2, 'iss_scms_id': 'DEF'}],
                      [{'rsu_id': 3, 'iss_scms_id': 'GHI'}]
                    ]
                )
              )
            )
          )
        ),
        __exit__ = MagicMock()
      )
    )
  )

  # run
  result = pgquery.query_db("SELECT * FROM test")

  expected_result = [
      [{'rsu_id': 1, 'iss_scms_id': 'ABC'}],
      [{'rsu_id': 2, 'iss_scms_id': 'DEF'}],
      [{'rsu_id': 3, 'iss_scms_id': 'GHI'}]
    ]
  assert result == expected_result
  mock_init_tcp_connection_engine.assert_called_once()

@patch('images.iss_health_check.pgquery.db', new=None)
@patch('images.iss_health_check.pgquery.init_tcp_connection_engine')
def test_query_db_return(mock_init_tcp_connection_engine):
  # mock
  mock_init_tcp_connection_engine.return_value = Mock( # returns db
    connect=MagicMock(
      return_value=Mock( # returns connection
        __enter__=MagicMock(
          return_value=Mock( # returns connection iterator
            execute=MagicMock(
              return_value=Mock( # returns data result
                fetchall=MagicMock(
                  return_value =  [
                      [{'rsu_id': 1, 'iss_scms_id': 'ABC'}],
                      [{'rsu_id': 2, 'iss_scms_id': 'DEF'}],
                      [{'rsu_id': 3, 'iss_scms_id': 'GHI'}]
                    ]
                )
              )
            )
          )
        ),
        __exit__ = MagicMock()
      )
    )
  )

  # run
  result = pgquery.query_db("SELECT * FROM test")

  expected_result = [
      [{'rsu_id': 1, 'iss_scms_id': 'ABC'}],
      [{'rsu_id': 2, 'iss_scms_id': 'DEF'}],
      [{'rsu_id': 3, 'iss_scms_id': 'GHI'}]
    ]
  assert result == expected_result
  mock_init_tcp_connection_engine.assert_called_once()

@patch('images.iss_health_check.pgquery.db', new=None)
@patch('images.iss_health_check.pgquery.init_tcp_connection_engine')
def test_query_db_no_return(mock_init_tcp_connection_engine):
  # mock
  mock_init_tcp_connection_engine.return_value = Mock( # returns db
    connect=MagicMock(
      return_value=Mock( # returns connection
        __enter__=MagicMock(
          return_value=Mock( # returns connection iterator
            execute=MagicMock(
              return_value=Mock( # returns data result
                fetchall=MagicMock(
                  return_value =  [
                      [{'rsu_id': 1, 'iss_scms_id': 'ABC'}],
                      [{'rsu_id': 2, 'iss_scms_id': 'DEF'}],
                      [{'rsu_id': 3, 'iss_scms_id': 'GHI'}]
                    ]
                )
              )
            )
          )
        ),
        __exit__ = MagicMock()
      )
    )
  )

  # run
  result = pgquery.query_db("SELECT * FROM test", True)

  assert result == None
  mock_init_tcp_connection_engine.assert_called_once()