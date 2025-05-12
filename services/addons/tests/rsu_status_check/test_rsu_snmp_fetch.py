from mock import patch
from addons.images.rsu_status_check import rsu_msgfwd_fetch
from addons.tests.rsu_status_check.data import rsu_snmp_fetch_data


@patch("addons.images.rsu_status_check.rsu_msgfwd_fetch.pgquery.query_db")
def test_get_rsu_list(mock_query_db):
    mock_query_db.return_value = rsu_snmp_fetch_data.query_rsu_list

    # call
    result = rsu_msgfwd_fetch.get_rsu_list()

    # verify
    mock_query_db.assert_called_with(rsu_snmp_fetch_data.get_rsu_list_query_string)
    assert result == rsu_snmp_fetch_data.rsu_list
