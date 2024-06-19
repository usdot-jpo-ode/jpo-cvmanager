from mock import patch
from common import email_util

@patch("common.email_util.query_db")
def test_get_email_list(mock_query_db):
    mock_query_db.return_value = [[{"email": "bob@gmail.com"},], [{"email": "bob2@gmail.com"}]]
    email_list = email_util.get_email_list("msg_type")
    assert email_list == ["bob@gmail.com", "bob2@gmail.com"]