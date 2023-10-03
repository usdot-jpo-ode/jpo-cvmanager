from unittest.mock import MagicMock, Mock, patch
from addons.images.count_metric import pgquery_rsu
import os


@patch("addons.images.count_metric.pgquery_rsu.sqlalchemy")
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
    result = pgquery_rsu.init_tcp_connection_engine()

    # check
    mock_sqlalchemy.create_engine.assert_called_once()
    mock_engine.dialect.description_encoding = None
    assert result == mock_engine


@patch("addons.images.count_metric.pgquery_rsu.db", new=None)
@patch("addons.images.count_metric.pgquery_rsu.init_tcp_connection_engine")
def test_get_rsu_data(mock_init_tcp_connection_engine):
    # mock
    mock_init_tcp_connection_engine.return_value = Mock(  # returns db
        connect=MagicMock(
            return_value=Mock(  # returns connection
                __enter__=MagicMock(
                    return_value=Mock(  # returns connection iterator
                        execute=MagicMock(
                            return_value=Mock(  # returns data result
                                fetchall=MagicMock(return_value=[["ipaddr", "proute"]])
                            )
                        )
                    )
                ),
                __exit__=MagicMock(),
            )
        )
    )

    # run
    result = pgquery_rsu.get_rsu_data()

    expected_result = [{"ipAddress": "ipaddr", "primaryRoute": "proute"}]
    assert result == expected_result
    mock_init_tcp_connection_engine.assert_called_once()
