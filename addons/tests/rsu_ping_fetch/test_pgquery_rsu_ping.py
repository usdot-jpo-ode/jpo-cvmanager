from unittest.mock import MagicMock, Mock, patch
from images.rsu_ping_fetch import pgquery_rsu
from datetime import datetime
from freezegun import freeze_time
import os

@patch('images.rsu_ping_fetch.pgquery_rsu.sqlalchemy')
def test_init_tcp_connection_engine(mock_sqlalchemy):
    # mock environment
    db_user = 'db_user'
    db_pass = 'db_pass'
    db_name = 'db_name'
    db_host = '127.0.0.1:1234'

    os.environ['DB_USER'] = db_user
    os.environ['DB_PASS'] = db_pass
    os.environ['DB_NAME'] = db_name
    os.environ['DB_HOST'] = db_host

    # mock sqlalchemy
    mock_engine = MagicMock()
    mock_engine.dialect.description_encoding = None
    mock_sqlalchemy.create_engine.return_value = mock_engine

    # call
    result = pgquery_rsu.init_tcp_connection_engine()

    # check
    mock_sqlalchemy.create_engine.assert_called_once()
    mock_engine.dialect.description_encoding = None
    assert(result == mock_engine)

@patch('images.rsu_ping_fetch.pgquery_rsu.db', new=None)
@patch('images.rsu_ping_fetch.pgquery_rsu.init_tcp_connection_engine')
def test_get_rsu_data(mock_init_tcp_connection_engine):
    # mock
    mock_init_tcp_connection_engine.return_value = Mock( # returns db
        connect=MagicMock(
            return_value=Mock( # returns connection
                __enter__=MagicMock(
                    return_value=Mock( # returns connection iterator
                        execute=MagicMock(
                            return_value=Mock( # returns data result
                                fetchall=MagicMock(
                                    return_value = [
                                        [1, 'ipaddr']
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
    result = pgquery_rsu.get_rsu_data()

    expected_result = [{'rsu_id': 1, 'rsu_ip': 'ipaddr'}]
    assert result == expected_result
    mock_init_tcp_connection_engine.assert_called_once()

@patch('images.rsu_ping_fetch.pgquery_rsu.init_tcp_connection_engine')
def test_insert_rsu_ping(mock_init_tcp_connection_engine):
    # mock
    mock_init_tcp_connection_engine.return_value = Mock( # returns db
        connect=MagicMock(
            return_value=Mock( # returns connection
                __enter__=MagicMock(
                    return_value=Mock( # returns connection iterator
                        execute=MagicMock(
                            return_value=Mock( # returns data result
                                fetchall=MagicMock(
                                    return_value = [
                                        [1, 'ipaddr']
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
    result = pgquery_rsu.get_rsu_data()

    expected_result = [{'rsu_id': 1, 'rsu_ip': 'ipaddr'}]
    assert result == expected_result
    mock_init_tcp_connection_engine.assert_called_once()

@patch('images.rsu_ping_fetch.pgquery_rsu.db', new=None)
@patch('images.rsu_ping_fetch.pgquery_rsu.init_tcp_connection_engine')
def test_insert_rsu_ping(mock_init_tcp_connection_engine):
    # mock
    mock_engine = MagicMock()
    mock_engine.connect.return_value = MagicMock()
    mock_engine.connect.return_value.__enter__.return_value = MagicMock()
    mock_init_tcp_connection_engine.return_value = mock_engine

    # call
    testJson = {
        'histories': [
            {
                'itemid': '487682', 
                'clock': '1632350648', 
                'value': '1', 
                'ns': '447934900'
            }, 
            {
                'itemid': '487682', 
                'clock': '1632350348', 
                'value': '1', 
                'ns': '310686112'
            }, 
            {
                'itemid': '487682', 
                'clock': '1632350048', 
                'value': '1', 
                'ns': '537353876'
            }, 
            {
                'itemid': '487682', 
                'clock': '1632349748', 
                'value': '1', 
                'ns': '825216963'
            }, 
            {
                'itemid': '487682', 
                'clock': '1632349448', 
                'value': '1', 
                'ns': '555282271'
            }
        ], 
        'rsu_id': 230, 
        'rsu_ip': '172.16.28.51'
    }
    result = pgquery_rsu.insert_rsu_ping(testJson)
    
    # check
    mock_init_tcp_connection_engine.assert_called_once()
    mock_engine.connect.assert_called_once()
    assert(result == True)

@patch('images.rsu_ping_fetch.pgquery_rsu.db', new=None)
@patch('images.rsu_ping_fetch.pgquery_rsu.init_tcp_connection_engine')
def test_run_query(mock_init_tcp_connection_engine):
    # mock
    mock_engine = MagicMock()
    mock_engine.connect.return_value = MagicMock()
    mock_engine.connect.return_value.__enter__.return_value = MagicMock()
    mock_init_tcp_connection_engine.return_value = mock_engine

    # call
    query = "DELETE FROM public.ping WHERE rsu_id = 0 AND timestamp < \'2023/07/05T00:00:00\'::timestamp'"
    result = pgquery_rsu.run_query(query)
    
    # check
    mock_init_tcp_connection_engine.assert_called_once()
    mock_engine.connect.assert_called_once()
    assert(result == True)

@freeze_time("2023-07-06")
@patch('images.rsu_ping_fetch.pgquery_rsu.db', new=None)
@patch('images.rsu_ping_fetch.pgquery_rsu.init_tcp_connection_engine')
def test_get_last_online_rsu_records(mock_init_tcp_connection_engine):
    # mock
    mock_engine = MagicMock()
    mock_engine.connect.return_value = MagicMock()
    mock_engine.connect.return_value.__enter__.return_value = MagicMock()
    mock_init_tcp_connection_engine.return_value = Mock( # returns db
        connect=MagicMock(
            return_value=Mock( # returns connection
                __enter__=MagicMock(
                    return_value=Mock( # returns connection iterator
                        execute=MagicMock(
                            return_value=Mock( # returns data result
                                fetchall=MagicMock(
                                    return_value = [
                                        [1, 1, datetime.now()]
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

    # call
    result = pgquery_rsu.get_last_online_rsu_records()

    # check
    mock_init_tcp_connection_engine.assert_called_once()
    assert(len(result) == 1)
    assert(result[0][0] == 1)
    assert(result[0][1] == 1)
    assert(result[0][2].strftime("%Y/%m/%d") == '2023/07/06')