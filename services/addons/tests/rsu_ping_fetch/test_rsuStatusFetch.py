from mock import call, MagicMock, patch
from addons.images.rsu_ping_fetch import rsuStatusFetch


@patch("addons.images.rsu_ping_fetch.purger.pgquery.query_db")
def test_get_rsu_data(mock_query_db):
    # mock
    mock_query_db.return_value = [(1, 'ipaddr')]

    # run
    result = rsuStatusFetch.get_rsu_data()

    expected_result = [{'rsu_id': 1, 'rsu_ip': 'ipaddr'}]
    assert result == expected_result
    mock_query_db.assert_called_once()

@patch("addons.images.rsu_ping_fetch.purger.pgquery.write_db")
def test_insert_rsu_ping(mock_write_db):
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
    rsuStatusFetch.insert_rsu_ping(testJson)
    
    # check
    expected_calls = [
        call('INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp(1632350648), B\'1\', 230)'),
        call('INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp(1632350348), B\'1\', 230)'),
        call('INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp(1632350048), B\'1\', 230)'),
        call('INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp(1632349748), B\'1\', 230)'),
        call('INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp(1632349448), B\'1\', 230)')
    ]
    mock_write_db.assert_has_calls(expected_calls)


def createRsuStatusFetchInstance():
    rsuStatusFetch.os.environ['ZABBIX_ENDPOINT'] = 'endpoint'
    rsuStatusFetch.os.environ['ZABBIX_USER'] = 'user'
    rsuStatusFetch.os.environ['ZABBIX_PASSWORD'] = 'password'
    return rsuStatusFetch.RsuStatusFetch()

def test_setZabbixAuth():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsuStatusFetch.logging.info = MagicMock()
    rsuStatusFetch.requests.post = MagicMock()
    rsuStatusFetch.requests.post.return_value.json.return_value = {'result': 'auth'}

    # call
    rsf.setZabbixAuth()

    # check
    rsuStatusFetch.logging.info.assert_called_once_with('Fetching Zabbix auth token from endpoint')
    rsuStatusFetch.requests.post.assert_called_once_with('endpoint', json={
        "jsonrpc": "2.0",
        "method": "user.login",
        "id": 1,
        "params": {
            "username": 'user',
            "password": 'password'
        }
    })
    assert(rsf.ZABBIX_AUTH == 'auth')

def test_getHostInfo():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.ZABBIX_AUTH = 'auth'
    rsuStatusFetch.requests.post = MagicMock()
    rsuStatusFetch.requests.post.return_value.json.return_value = {'result': 'result'}

    # call
    rsu_ip = 'testaddress'
    result = rsf.getHostInfo(rsu_ip)

    # check
    rsuStatusFetch.requests.post.assert_called_once_with(rsf.ZABBIX_ENDPOINT, json={
        "jsonrpc": "2.0",
        "method": "host.get",
        "id": 1,
        "auth": 'auth',
        "params": {
            "output": [
                "hostid",
                "host"
            ],
            "selectInterfaces": [
                "interfaceid",
                "ip"
            ],
            "filter": {
                "ip": "testaddress"
            }
        }

    })
    assert(result == {'result': 'result'})

def test_getItem():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.ZABBIX_AUTH = 'auth'
    rsuStatusFetch.requests.post = MagicMock()
    rsuStatusFetch.requests.post.return_value.json.return_value = {'result': 'result'}

    # call
    hostInfo = {
        "result": [
            {
                "hostid": "hostid",
            }
        ]
    }
    result = rsf.getItem(hostInfo)

    # check
    rsuStatusFetch.requests.post.assert_called_once_with(rsf.ZABBIX_ENDPOINT, json={
        "jsonrpc": "2.0",
        "method": "item.get",
        "id": 1,
        "auth": 'auth',
        "params": {
            "hostids": ["hostid"],
            "filter": {"key_": "icmpping"}
        }
    })
    assert(result == {'result': 'result'})

def test_getHistory():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.ZABBIX_AUTH = 'auth'
    rsuStatusFetch.requests.post = MagicMock()
    rsuStatusFetch.requests.post.return_value.json.return_value = {'result': 'result'}

    # call
    zabbix_item = {
        "result": [
            {
                "itemid": "itemid",
            }
        ]
    }
    result = rsf.getHistory(zabbix_item)

    # check
    rsuStatusFetch.requests.post.assert_called_once_with(rsf.ZABBIX_ENDPOINT, json={
        "jsonrpc": "2.0",
        "method": "history.get",
        "id": 1,
        "auth": 'auth',
        "params": {
            "itemids": ["itemid"],
            "output": "extend",
            "sortfield": "clock",
            "sortorder": "DESC",
            "limit": 5
        }
    })
    assert(result == {'result': 'result'})

def test_insertHistoryItem():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsuStatusFetch.insert_rsu_ping = MagicMock(return_value=True)
    rsuStatusFetch.logging.info = MagicMock()
    rsuStatusFetch.requests.post = MagicMock()
    rsuStatusFetch.requests.post.return_value.status_code = 200

    # call
    zabbix_history = {
        "result": {
            "itemid": "itemid",
        }
    }
    rsuInfo = {
        "rsu_id": 1,
        "rsu_ip": "testaddress"
    }
    result = rsf.insertHistoryItem(zabbix_history, rsuInfo)

    # check
    expected_json = {'histories': {'itemid': 'itemid'}, 'rsuData': {'rsu_id': 1, 'rsu_ip': 'testaddress'}}
    rsuStatusFetch.insert_rsu_ping(expected_json)
    rsuStatusFetch.logging.info.assert_called_once_with('Inserting 1 history items for RSU testaddress')
    assert(result == True)

def test_printConfigInfo():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsuStatusFetch.logging.info = MagicMock()

    # call
    rsf.printConfigInfo()

    # check
    expected_config_object = {
        'ZABBIX_ENDPOINT': 'endpoint',
        'ZABBIX_AUTH': ''
    }
    expected_message = 'Configuration: ' + str(expected_config_object)
    rsuStatusFetch.logging.info.assert_called_once_with(expected_message)

def test_run():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.setZabbixAuth = MagicMock()
    rsf.printConfigInfo = MagicMock()
    rsuStatusFetch.get_rsu_data = MagicMock()
    rsuStatusFetch.get_rsu_data.return_value = [
        {
            "rsu_id": 1,
            "rsu_ip": "testaddress"
        }
    ]
    rsuStatusFetch.logging.info = MagicMock()
    rsf.getHostInfo = MagicMock()
    rsf.getItem = MagicMock()
    rsf.getHistory = MagicMock()
    rsf.insertHistoryItem = MagicMock()
    rsf.insertHistoryItem.return_value = True
    rsuStatusFetch.logging.warning = MagicMock()
    rsuStatusFetch.logging.error = MagicMock()

    # call
    rsf.run()

    # check
    rsf.setZabbixAuth.assert_called_once()
    rsf.printConfigInfo.assert_called_once()
    rsuStatusFetch.get_rsu_data.assert_called_once()
    rsf.getHostInfo.assert_called_once()
    rsf.getItem.assert_called_once()
    rsf.getHistory.assert_called_once()
    rsf.insertHistoryItem.assert_called_once()
    rsuStatusFetch.logging.warning.assert_not_called()
    rsuStatusFetch.logging.error.assert_not_called()

def test_run_insert_failure():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.setZabbixAuth = MagicMock()
    rsf.printConfigInfo = MagicMock()
    rsuStatusFetch.get_rsu_data = MagicMock()
    rsuStatusFetch.get_rsu_data.return_value = [
        {
            "rsu_id": 1,
            "rsu_ip": "testaddress"
        }
    ]
    rsuStatusFetch.logging.info = MagicMock()
    rsf.getHostInfo = MagicMock()
    rsf.getItem = MagicMock()
    rsf.getHistory = MagicMock()
    rsf.insertHistoryItem = MagicMock()
    rsf.insertHistoryItem.return_value = False
    rsuStatusFetch.logging.warning = MagicMock()
    rsuStatusFetch.logging.error = MagicMock()

    # call
    rsf.run()

    # check
    rsf.setZabbixAuth.assert_called_once()
    rsf.printConfigInfo.assert_called_once()
    rsuStatusFetch.get_rsu_data.assert_called_once()
    rsf.getHostInfo.assert_called_once()
    rsf.getItem.assert_called_once()
    rsf.getHistory.assert_called_once()
    rsf.insertHistoryItem.assert_called_once()
    rsuStatusFetch.logging.warning.assert_called_once_with('Failed to insert history item for testaddress')
    rsuStatusFetch.logging.error.assert_not_called()

def test_run_exception():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.setZabbixAuth = MagicMock()
    rsf.printConfigInfo = MagicMock()
    rsuStatusFetch.get_rsu_data = MagicMock()
    rsuStatusFetch.get_rsu_data.return_value = [
        {
            "rsu_id": 1,
            "rsu_ip": "testaddress"
        }
    ]
    rsuStatusFetch.logging.info = MagicMock()
    rsf.getHostInfo = MagicMock()
    rsf.getHostInfo.side_effect = Exception('test exception')
    rsuStatusFetch.logging.warning = MagicMock()
    rsuStatusFetch.logging.error = MagicMock()

    # call
    rsf.run()

    # check
    rsf.setZabbixAuth.assert_called_once()
    rsf.printConfigInfo.assert_called_once()
    rsuStatusFetch.get_rsu_data.assert_called_once()
    rsuStatusFetch.logging.info.assert_called_once_with('Found 1 RSUs to fetch status for')
    rsf.getHostInfo.assert_called_once()
    rsuStatusFetch.logging.warning.assert_not_called()
    rsuStatusFetch.logging.error.assert_called_once_with('Failed to fetch Zabbix data RSU testaddress')
