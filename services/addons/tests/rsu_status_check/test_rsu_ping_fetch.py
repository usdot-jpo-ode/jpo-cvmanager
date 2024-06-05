from mock import call, MagicMock, patch
from addons.images.rsu_status_check import rsu_ping_fetch


@patch("addons.images.rsu_status_check.purger.pgquery.query_db")
def test_get_rsu_data(mock_query_db):
    # mock
    mock_query_db.return_value = [(1, "ipaddr")]

    # run
    result = rsu_ping_fetch.get_rsu_data()

    expected_result = [{"rsu_id": 1, "rsu_ip": "ipaddr"}]
    assert result == expected_result
    mock_query_db.assert_called_once()


@patch("addons.images.rsu_status_check.purger.pgquery.write_db")
def test_insert_rsu_ping(mock_write_db):
    # call
    testJson = {
        "histories": [
            {
                "itemid": "487682",
                "clock": "1632350648",
                "value": "1",
                "ns": "447934900",
            },
            {
                "itemid": "487682",
                "clock": "1632350348",
                "value": "1",
                "ns": "310686112",
            },
            {
                "itemid": "487682",
                "clock": "1632350048",
                "value": "1",
                "ns": "537353876",
            },
            {
                "itemid": "487682",
                "clock": "1632349748",
                "value": "1",
                "ns": "825216963",
            },
            {
                "itemid": "487682",
                "clock": "1632349448",
                "value": "1",
                "ns": "555282271",
            },
        ],
        "rsu_id": 230,
        "rsu_ip": "172.16.28.51",
    }
    rsu_ping_fetch.insert_rsu_ping(testJson)

    # check
    expected_calls = [
        call(
            "INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp(1632350648), B'1', 230)"
        ),
        call(
            "INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp(1632350348), B'1', 230)"
        ),
        call(
            "INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp(1632350048), B'1', 230)"
        ),
        call(
            "INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp(1632349748), B'1', 230)"
        ),
        call(
            "INSERT INTO public.ping (timestamp, result, rsu_id) VALUES (to_timestamp(1632349448), B'1', 230)"
        ),
    ]
    mock_write_db.assert_has_calls(expected_calls)


def createRsuStatusFetchInstance():
    rsu_ping_fetch.os.environ["ZABBIX_ENDPOINT"] = "endpoint"
    rsu_ping_fetch.os.environ["ZABBIX_USER"] = "user"
    rsu_ping_fetch.os.environ["ZABBIX_PASSWORD"] = "password"
    return rsu_ping_fetch.RsuStatusFetch()


def test_setZabbixAuth():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsu_ping_fetch.logging.info = MagicMock()
    rsu_ping_fetch.requests.post = MagicMock()
    rsu_ping_fetch.requests.post.return_value.json.return_value = {"result": "auth"}

    # call
    rsf.setZabbixAuth()

    # check
    rsu_ping_fetch.logging.info.assert_called_once_with(
        "Fetching Zabbix auth token from endpoint"
    )
    rsu_ping_fetch.requests.post.assert_called_once_with(
        "endpoint",
        json={
            "jsonrpc": "2.0",
            "method": "user.login",
            "id": 1,
            "params": {"username": "user", "password": "password"},
        },
    )
    assert rsf.ZABBIX_AUTH == "auth"


def test_getHostInfo():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.ZABBIX_AUTH = "auth"
    rsu_ping_fetch.requests.post = MagicMock()
    rsu_ping_fetch.requests.post.return_value.json.return_value = {"result": "result"}

    # call
    rsu_ip = "testaddress"
    result = rsf.getHostInfo(rsu_ip)

    # check
    rsu_ping_fetch.requests.post.assert_called_once_with(
        rsf.ZABBIX_ENDPOINT,
        json={
            "jsonrpc": "2.0",
            "method": "host.get",
            "id": 1,
            "auth": "auth",
            "params": {
                "output": ["hostid", "host"],
                "selectInterfaces": ["interfaceid", "ip"],
                "filter": {"ip": "testaddress"},
            },
        },
    )
    assert result == {"result": "result"}


def test_getItem():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.ZABBIX_AUTH = "auth"
    rsu_ping_fetch.requests.post = MagicMock()
    rsu_ping_fetch.requests.post.return_value.json.return_value = {"result": "result"}

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
    rsu_ping_fetch.requests.post.assert_called_once_with(
        rsf.ZABBIX_ENDPOINT,
        json={
            "jsonrpc": "2.0",
            "method": "item.get",
            "id": 1,
            "auth": "auth",
            "params": {"hostids": ["hostid"], "filter": {"key_": "icmpping"}},
        },
    )
    assert result == {"result": "result"}


def test_getHistory():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.ZABBIX_AUTH = "auth"
    rsu_ping_fetch.requests.post = MagicMock()
    rsu_ping_fetch.requests.post.return_value.json.return_value = {"result": "result"}

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
    rsu_ping_fetch.requests.post.assert_called_once_with(
        rsf.ZABBIX_ENDPOINT,
        json={
            "jsonrpc": "2.0",
            "method": "history.get",
            "id": 1,
            "auth": "auth",
            "params": {
                "itemids": ["itemid"],
                "output": "extend",
                "sortfield": "clock",
                "sortorder": "DESC",
                "limit": 5,
            },
        },
    )
    assert result == {"result": "result"}


def test_insertHistoryItem():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsu_ping_fetch.insert_rsu_ping = MagicMock(return_value=True)
    rsu_ping_fetch.logging.info = MagicMock()
    rsu_ping_fetch.requests.post = MagicMock()
    rsu_ping_fetch.requests.post.return_value.status_code = 200

    # call
    zabbix_history = {
        "result": {
            "itemid": "itemid",
        }
    }
    rsuInfo = {"rsu_id": 1, "rsu_ip": "testaddress"}
    result = rsf.insertHistoryItem(zabbix_history, rsuInfo)

    # check
    expected_json = {
        "histories": {"itemid": "itemid"},
        "rsuData": {"rsu_id": 1, "rsu_ip": "testaddress"},
    }
    rsu_ping_fetch.insert_rsu_ping(expected_json)
    rsu_ping_fetch.logging.info.assert_called_once_with(
        "Inserting 1 history items for RSU testaddress"
    )
    assert result == True


def test_printConfigInfo():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsu_ping_fetch.logging.info = MagicMock()

    # call
    rsf.printConfigInfo()

    # check
    expected_config_object = {"ZABBIX_ENDPOINT": "endpoint", "ZABBIX_AUTH": ""}
    expected_message = "Configuration: " + str(expected_config_object)
    rsu_ping_fetch.logging.info.assert_called_once_with(expected_message)


def test_run():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.setZabbixAuth = MagicMock()
    rsf.printConfigInfo = MagicMock()
    rsu_ping_fetch.get_rsu_data = MagicMock()
    rsu_ping_fetch.get_rsu_data.return_value = [{"rsu_id": 1, "rsu_ip": "testaddress"}]
    rsu_ping_fetch.logging.info = MagicMock()
    rsf.getHostInfo = MagicMock()
    rsf.getItem = MagicMock()
    rsf.getHistory = MagicMock()
    rsf.insertHistoryItem = MagicMock()
    rsf.insertHistoryItem.return_value = True
    rsu_ping_fetch.logging.warning = MagicMock()
    rsu_ping_fetch.logging.error = MagicMock()

    # call
    rsf.run()

    # check
    rsf.setZabbixAuth.assert_called_once()
    rsf.printConfigInfo.assert_called_once()
    rsu_ping_fetch.get_rsu_data.assert_called_once()
    rsf.getHostInfo.assert_called_once()
    rsf.getItem.assert_called_once()
    rsf.getHistory.assert_called_once()
    rsf.insertHistoryItem.assert_called_once()
    rsu_ping_fetch.logging.warning.assert_not_called()
    rsu_ping_fetch.logging.error.assert_not_called()


def test_run_insert_failure():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.setZabbixAuth = MagicMock()
    rsf.printConfigInfo = MagicMock()
    rsu_ping_fetch.get_rsu_data = MagicMock()
    rsu_ping_fetch.get_rsu_data.return_value = [{"rsu_id": 1, "rsu_ip": "testaddress"}]
    rsu_ping_fetch.logging.info = MagicMock()
    rsf.getHostInfo = MagicMock()
    rsf.getItem = MagicMock()
    rsf.getHistory = MagicMock()
    rsf.insertHistoryItem = MagicMock()
    rsf.insertHistoryItem.return_value = False
    rsu_ping_fetch.logging.warning = MagicMock()
    rsu_ping_fetch.logging.error = MagicMock()

    # call
    rsf.run()

    # check
    rsf.setZabbixAuth.assert_called_once()
    rsf.printConfigInfo.assert_called_once()
    rsu_ping_fetch.get_rsu_data.assert_called_once()
    rsf.getHostInfo.assert_called_once()
    rsf.getItem.assert_called_once()
    rsf.getHistory.assert_called_once()
    rsf.insertHistoryItem.assert_called_once()
    rsu_ping_fetch.logging.warning.assert_called_once_with(
        "Failed to insert history item for testaddress"
    )
    rsu_ping_fetch.logging.error.assert_not_called()


def test_run_exception():
    # prepare
    rsf = createRsuStatusFetchInstance()
    rsf.setZabbixAuth = MagicMock()
    rsf.printConfigInfo = MagicMock()
    rsu_ping_fetch.get_rsu_data = MagicMock()
    rsu_ping_fetch.get_rsu_data.return_value = [{"rsu_id": 1, "rsu_ip": "testaddress"}]
    rsu_ping_fetch.logging.info = MagicMock()
    rsf.getHostInfo = MagicMock()
    rsf.getHostInfo.side_effect = Exception("test exception")
    rsu_ping_fetch.logging.warning = MagicMock()
    rsu_ping_fetch.logging.error = MagicMock()

    # call
    rsf.run()

    # check
    rsf.setZabbixAuth.assert_called_once()
    rsf.printConfigInfo.assert_called_once()
    rsu_ping_fetch.get_rsu_data.assert_called_once()
    rsu_ping_fetch.logging.info.assert_called_once_with(
        "Found 1 RSUs to fetch status for"
    )
    rsf.getHostInfo.assert_called_once()
    rsu_ping_fetch.logging.warning.assert_not_called()
    rsu_ping_fetch.logging.error.assert_called_once_with(
        "Failed to fetch Zabbix data RSU testaddress"
    )
