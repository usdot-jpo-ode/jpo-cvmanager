from unittest.mock import patch, MagicMock
import os
import json

from addons.images.iss_health_check import iss_token

# --------------------- Storage Type tests ---------------------

@patch.dict(
    os.environ,
    {
        "STORAGE_TYPE": "gcp",
    },
)
def test_get_storage_type_gcp():
    actual_value = iss_token.get_storage_type()
    assert actual_value == "gcp"


@patch.dict(
    os.environ,
    {
        "STORAGE_TYPE": "postgres",
    },
)
def test_get_storage_type_postgres():
    actual_value = iss_token.get_storage_type()
    assert actual_value == "postgres"

@patch.dict(
    os.environ,
    {
        "STORAGE_TYPE": "test",
    },
)
def test_get_storage_type_invalid():
    try:
        iss_token.get_storage_type()
        assert False
    except SystemExit:
        assert True


@patch.dict(
    os.environ,
    {
        
    },
)
def test_get_storage_type_unset():
    try:
        iss_token.get_storage_type()
        assert False
    except SystemExit:
        assert True

# --------------------- end of Storage Type tests ---------------------
    

# --------------------- GCP tests ---------------------
@patch(
    "addons.images.iss_health_check.iss_token.secretmanager.SecretManagerServiceClient"
)
def test_create_secret(mock_sm_client):
    iss_token.create_secret(mock_sm_client, "test-secret_id", "test-parent")
    expected_request = {
        "parent": "test-parent",
        "secret_id": "test-secret_id",
        "secret": {"replication": {"automatic": {}}},
    }
    mock_sm_client.create_secret.assert_called_with(request=expected_request)


@patch(
    "addons.images.iss_health_check.iss_token.secretmanager.SecretManagerServiceClient"
)
@patch("addons.images.iss_health_check.iss_token.secretmanager")
def test_check_if_secret_exists_true(mock_secretmanager, mock_sm_client):
    mock_secretmanager.ListSecretsRequest.return_value = "list-request"

    item_match = MagicMock()
    item_match.name = "proj/test-proj/secret/test-secret_id"
    mock_list_values = [item_match]
    mock_sm_client.list_secrets.return_value = mock_list_values

    actual_value = iss_token.check_if_secret_exists(
        mock_sm_client, "test-secret_id", "test-parent"
    )
    mock_secretmanager.ListSecretsRequest.assert_called_with(parent="test-parent")
    mock_sm_client.list_secrets.assert_called_with(request="list-request")
    assert actual_value == True


@patch(
    "addons.images.iss_health_check.iss_token.secretmanager.SecretManagerServiceClient"
)
@patch("addons.images.iss_health_check.iss_token.secretmanager")
def test_check_if_secret_exists_false(mock_secretmanager, mock_sm_client):
    mock_secretmanager.ListSecretsRequest.return_value = "list-request"

    item_not_match = MagicMock()
    item_not_match.name = "proj/test-proj/secret/test-secret"
    mock_list_values = [item_not_match]
    mock_sm_client.list_secrets.return_value = mock_list_values

    actual_value = iss_token.check_if_secret_exists(
        mock_sm_client, "test-secret_id", "test-parent"
    )
    mock_secretmanager.ListSecretsRequest.assert_called_with(parent="test-parent")
    mock_sm_client.list_secrets.assert_called_with(request="list-request")
    assert actual_value == False


@patch(
    "addons.images.iss_health_check.iss_token.secretmanager.SecretManagerServiceClient"
)
def test_get_latest_secret_version(mock_sm_client):
    mock_response = MagicMock()
    mock_response.payload.data = str.encode('{"message": "Secret payload data"}')
    mock_sm_client.access_secret_version.return_value = mock_response

    actual_value = iss_token.get_latest_secret_version(
        mock_sm_client, "test-secret_id", "test-parent"
    )
    mock_sm_client.access_secret_version.assert_called_with(
        request={"name": "test-parent/secrets/test-secret_id/versions/latest"}
    )
    assert actual_value == {"message": "Secret payload data"}


@patch(
    "addons.images.iss_health_check.iss_token.secretmanager.SecretManagerServiceClient"
)
def test_add_secret_version(mock_sm_client):
    secret_id = "test-secret_id"
    parent = "test-parent"
    data = {"message": "Secret payload data"}
    iss_token.add_secret_version(mock_sm_client, secret_id, parent, data)

    expected_request = {
        "parent": f"{parent}/secrets/{secret_id}",
        "payload": {"data": str.encode(json.dumps(data))},
    }
    mock_sm_client.add_secret_version.assert_called_with(request=expected_request)


@patch.dict(
    os.environ,
    {
        "PROJECT_ID": "test-proj",
        "ISS_API_KEY": "test-api-key",
        "ISS_SCMS_TOKEN_REST_ENDPOINT": "https://api.dm.iss-scms.com/api/test-token",
        "ISS_API_KEY_NAME": "test-api-key-name",
        "STORAGE_TYPE": "gcp",
    },
)
@patch("addons.images.iss_health_check.iss_token.requests.Response")
@patch("addons.images.iss_health_check.iss_token.requests")
@patch("addons.images.iss_health_check.iss_token.uuid")
@patch("addons.images.iss_health_check.iss_token.add_secret_version")
@patch("addons.images.iss_health_check.iss_token.create_secret")
@patch("addons.images.iss_health_check.iss_token.check_if_secret_exists")
@patch("addons.images.iss_health_check.iss_token.secretmanager")
def test_get_token_create_secret(
    mock_secretmanager,
    mock_check_if_secret_exists,
    mock_create_secret,
    mock_add_secret_version,
    mock_uuid,
    mock_requests,
    mock_response,
):
    # Mock every major dependency
    mock_sm_client = MagicMock()
    mock_secretmanager.SecretManagerServiceClient.return_value = mock_sm_client
    mock_check_if_secret_exists.return_value = False
    mock_uuid.uuid4.return_value = 12345
    mock_requests.post.return_value = mock_response
    mock_response.json.return_value = {"Item": "new-iss-token"}

    # Call function
    expected_value = "new-iss-token"
    actual_value = iss_token.get_token()

    # Check if iss_token function calls were made correctly
    mock_check_if_secret_exists.assert_called_with(
        mock_sm_client, "iss-token-secret", "projects/test-proj"
    )
    mock_create_secret.assert_called_with(
        mock_sm_client, "iss-token-secret", "projects/test-proj"
    )
    mock_add_secret_version.assert_called_with(
        mock_sm_client,
        "iss-token-secret",
        "projects/test-proj",
        {"name": "test-api-key-name_12345", "token": expected_value},
    )

    # Check if HTTP requests were made correctly
    expected_headers = {"x-api-key": "test-api-key"}
    expected_body = {"friendlyName": "test-api-key-name_12345", "expireDays": 1}
    mock_requests.post.assert_called_with(
        "https://api.dm.iss-scms.com/api/test-token",
        json=expected_body,
        headers=expected_headers,
    )

    # Assert final value
    assert actual_value == expected_value


@patch.dict(
    os.environ,
    {
        "PROJECT_ID": "test-proj",
        "ISS_API_KEY": "test-api-key",
        "ISS_SCMS_TOKEN_REST_ENDPOINT": "https://api.dm.iss-scms.com/api/test-token",
        "ISS_API_KEY_NAME": "test-api-key-name",
        "STORAGE_TYPE": "gcp",
    },
)
@patch("addons.images.iss_health_check.iss_token.requests.Response")
@patch("addons.images.iss_health_check.iss_token.requests")
@patch("addons.images.iss_health_check.iss_token.uuid")
@patch("addons.images.iss_health_check.iss_token.add_secret_version")
@patch("addons.images.iss_health_check.iss_token.get_latest_secret_version")
@patch("addons.images.iss_health_check.iss_token.check_if_secret_exists")
@patch("addons.images.iss_health_check.iss_token.secretmanager")
def test_get_token_secret_exists(
    mock_secretmanager,
    mock_check_if_secret_exists,
    mock_get_latest_secret_version,
    mock_add_secret_version,
    mock_uuid,
    mock_requests,
    mock_response,
):
    # Mock every major dependency
    mock_sm_client = MagicMock()
    mock_secretmanager.SecretManagerServiceClient.return_value = mock_sm_client
    mock_check_if_secret_exists.return_value = True
    mock_get_latest_secret_version.return_value = {
        "name": "test-api-key-name_01234",
        "token": "old-token",
    }
    mock_uuid.uuid4.return_value = 12345
    mock_requests.post.return_value = mock_response
    mock_response.json.return_value = {"Item": "new-iss-token"}

    # Call function
    expected_value = "new-iss-token"
    actual_value = iss_token.get_token()

    # Check if iss_token function calls were made correctly
    mock_check_if_secret_exists.assert_called_with(
        mock_sm_client, "iss-token-secret", "projects/test-proj"
    )
    mock_get_latest_secret_version.assert_called_with(
        mock_sm_client, "iss-token-secret", "projects/test-proj"
    )
    mock_add_secret_version.assert_called_with(
        mock_sm_client,
        "iss-token-secret",
        "projects/test-proj",
        {"name": "test-api-key-name_12345", "token": expected_value},
    )

    # Check if HTTP requests were made correctly
    expected_headers = {"x-api-key": "old-token"}
    expected_post_body = {"friendlyName": "test-api-key-name_12345", "expireDays": 1}
    mock_requests.post.assert_called_with(
        "https://api.dm.iss-scms.com/api/test-token",
        json=expected_post_body,
        headers=expected_headers,
    )

    expected_delete_body = {"friendlyName": "test-api-key-name_01234"}
    mock_requests.delete.assert_called_with(
        "https://api.dm.iss-scms.com/api/test-token",
        json=expected_delete_body,
        headers=expected_headers,
    )

    # Assert final value
    assert actual_value == expected_value

# --------------------- end of GCP tests ---------------------
    

# --------------------- Postgres tests ---------------------

@patch(
    "addons.images.iss_health_check.iss_token.pgquery",
)
def test_check_if_data_exists_true(mock_pgquery):
    mock_pgquery.query_db.return_value = [(1,)]
    actual_value = iss_token.check_if_data_exists("test-table-name")
    expected_query = (
        "SELECT * FROM test-table-name"
    )
    mock_pgquery.query_db.assert_called_with(expected_query)
    assert actual_value == True


@patch(
    "addons.images.iss_health_check.iss_token.pgquery",
)
def test_check_if_data_exists_false(mock_pgquery):
    mock_pgquery.query_db.return_value = []
    actual_value = iss_token.check_if_data_exists("test-table-name")
    expected_query = (
        "SELECT * FROM test-table-name"
    )
    mock_pgquery.query_db.assert_called_with(expected_query)
    assert actual_value == False


@patch(
    "addons.images.iss_health_check.iss_token.pgquery",
)
def test_add_data(mock_pgquery):
    iss_token.add_data("test-table-name", "test-common-name", "test-token")
    expected_query = (
        "INSERT INTO test-table-name (common_name, token) "
        "VALUES ('test-common-name', 'test-token')"
    )
    mock_pgquery.write_db.assert_called_with(expected_query)


@patch(
    "addons.images.iss_health_check.iss_token.pgquery",
)
def test_get_latest_data(mock_pgquery):
    mock_pgquery.query_db.return_value = [(1, "test-common-name", "test-token")]
    actual_value = iss_token.get_latest_data("test-table-name")
    expected_query = (
        "SELECT * FROM test-table-name ORDER BY iss_key_id DESC LIMIT 1"
    )
    mock_pgquery.query_db.assert_called_with(expected_query)
    assert actual_value == {"id": 1, "name": "test-common-name", "token": "test-token"}


@patch.dict(
    os.environ,
    {
        "PROJECT_ID": "test-proj",
        "ISS_API_KEY": "test-api-key",
        "ISS_SCMS_TOKEN_REST_ENDPOINT": "https://api.dm.iss-scms.com/api/test-token",
        "ISS_API_KEY_NAME": "test-api-key-name",
        "STORAGE_TYPE": "postgres",
        "ISS_KEY_TABLE_NAME": "test-table-name",
    },
)
@patch("addons.images.iss_health_check.iss_token.requests.Response")
@patch("addons.images.iss_health_check.iss_token.requests")
@patch("addons.images.iss_health_check.iss_token.uuid")
@patch("addons.images.iss_health_check.iss_token.add_data")
@patch("addons.images.iss_health_check.iss_token.check_if_data_exists")
def test_get_token_data_does_not_exist(
    mock_check_if_data_exists,
    mock_add_data,
    mock_uuid,
    mock_requests,
    mock_response,
):
    # Mock every major dependency
    mock_check_if_data_exists.return_value = False
    mock_uuid.uuid4.return_value = 12345
    mock_requests.post.return_value = mock_response
    mock_response.json.return_value = {"Item": "new-iss-token"}

    # Call function
    result = iss_token.get_token()

    # Check if iss_token function calls were made correctly
    mock_check_if_data_exists.assert_called_with("test-table-name")
    mock_add_data.assert_called_with(
        "test-table-name", "test-api-key-name_12345", "new-iss-token"
    )

    # Check if HTTP requests were made correctly
    expected_headers = {"x-api-key": "test-api-key"}
    expected_post_body = {"friendlyName": "test-api-key-name_12345", "expireDays": 1}
    mock_requests.post.assert_called_with(
        "https://api.dm.iss-scms.com/api/test-token",
        json=expected_post_body,
        headers=expected_headers,
    )

    # Assert final value
    assert result == "new-iss-token"


@patch.dict(
    os.environ,
    {
        "PROJECT_ID": "test-proj",
        "ISS_API_KEY": "test-api-key",
        "ISS_SCMS_TOKEN_REST_ENDPOINT": "https://api.dm.iss-scms.com/api/test-token",
        "ISS_API_KEY_NAME": "test-api-key-name",
        "STORAGE_TYPE": "postgres",
        "ISS_KEY_TABLE_NAME": "test-table-name",
    },
)
@patch("addons.images.iss_health_check.iss_token.requests.Response")
@patch("addons.images.iss_health_check.iss_token.requests")
@patch("addons.images.iss_health_check.iss_token.uuid")
@patch("addons.images.iss_health_check.iss_token.add_data")
@patch("addons.images.iss_health_check.iss_token.check_if_data_exists")
@patch("addons.images.iss_health_check.iss_token.get_latest_data")
def test_get_token_data_exists(
    mock_get_latest_data,
    mock_check_if_data_exists,
    mock_add_data,
    mock_uuid,
    mock_requests,
    mock_response,
):
    # Mock every major dependency
    mock_check_if_data_exists.return_value = True
    mock_get_latest_data.return_value = {   
        "id": 1,
        "name": "test-api-key-name_01234",
        "token": "old-token",
    }
    mock_uuid.uuid4.return_value = 12345
    mock_requests.post.return_value = mock_response
    mock_response.json.return_value = {"Item": "new-iss-token"}

    # Call function
    result = iss_token.get_token()

    # Check if iss_token function calls were made correctly
    mock_check_if_data_exists.assert_called_with("test-table-name")
    mock_get_latest_data.assert_called_with("test-table-name")
    mock_add_data.assert_called_with(
        "test-table-name", "test-api-key-name_12345", "new-iss-token"
    )

    # Check if HTTP requests were made correctly
    expected_headers = {"x-api-key": "old-token"}
    expected_post_body = {"friendlyName": "test-api-key-name_12345", "expireDays": 1}
    mock_requests.post.assert_called_with(
        "https://api.dm.iss-scms.com/api/test-token",
        json=expected_post_body,
        headers=expected_headers,
    )

    expected_delete_body = {"friendlyName": "test-api-key-name_01234"}
    mock_requests.delete.assert_called_with(
        "https://api.dm.iss-scms.com/api/test-token",
        json=expected_delete_body,
        headers=expected_headers,
    )

    # Assert final value
    assert result == "new-iss-token"

# --------------------- end of Postgres tests ---------------------