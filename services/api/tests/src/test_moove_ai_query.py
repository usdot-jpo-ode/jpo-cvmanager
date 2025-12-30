from unittest.mock import patch, MagicMock

import pytest
import api.src.moove_ai_query as moove_ai_query
import api.tests.data.moove_ai_query_data as moove_ai_query_data
import pandas as pd
from werkzeug.exceptions import InternalServerError, BadRequest

###################################### Testing Requests ##########################################


def test_request_options():
    mooveai = moove_ai_query.MooveAiData()
    (body, code, headers) = mooveai.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "POST"


@patch("api.src.moove_ai_query.query_moove_ai")
def test_entry_post(mock_query_moove_ai):
    req = MagicMock()
    req.json = moove_ai_query_data.request_json_good
    mock_query_moove_ai.return_value = []
    with patch("api.src.moove_ai_query.request", req):
        mooveai = moove_ai_query.MooveAiData()
        (body, code, headers) = mooveai.post()

        mock_query_moove_ai.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == []


@patch("api.src.moove_ai_query.query_moove_ai")
def test_entry_post_bad_req(mock_query_moove_ai):
    req = MagicMock()
    req.json = moove_ai_query_data.request_json_bad
    mock_query_moove_ai.return_value = []
    with patch("api.src.moove_ai_query.request", req):
        mooveai = moove_ai_query.MooveAiData()

        with pytest.raises(BadRequest) as exc_info:
            (body, code, headers) = mooveai.post()

        assert (
            str(exc_info.value)
            == "400 Bad Request: {'geometry': ['Not a valid list.']}"
        )


###################################### Testing Functions ##########################################
@patch("api_environment.GCP_PROJECT_ID", "test_project")
@patch(
    "api_environment.MOOVE_AI_SEGMENT_AGG_STATS_TABLE",
    "test_segment_agg_stats_table",
)
@patch(
    "api_environment.MOOVE_AI_SEGMENT_EVENT_STATS_TABLE",
    "test_segment_event_stats_table",
)
@patch("api.src.moove_ai_query.bigquery")
def test_query_moove_ai(mock_bigquery):
    mock_bq_client = MagicMock()
    mock_bigquery.Client.return_value = mock_bq_client
    mock_query_resp = MagicMock()
    mock_bq_client.query.return_value = mock_query_resp
    mock_query_resp.to_dataframe.return_value = pd.DataFrame(
        moove_ai_query_data.query_return_data
    )

    resp = moove_ai_query.query_moove_ai(moove_ai_query_data.coordinate_list)

    mock_bq_client.query.assert_called_once_with(moove_ai_query_data.expected_bq_query)
    assert resp == moove_ai_query_data.feature_list


@patch("api_environment.GCP_PROJECT_ID", "test_project")
@patch(
    "api_environment.MOOVE_AI_SEGMENT_AGG_STATS_TABLE",
    "test_segment_agg_stats_table",
)
@patch(
    "api_environment.MOOVE_AI_SEGMENT_EVENT_STATS_TABLE",
    "test_segment_event_stats_table",
)
@patch("api.src.moove_ai_query.bigquery")
def test_query_moove_ai_exception(mock_bigquery):
    mock_bq_client = MagicMock()
    mock_bigquery.Client.return_value = mock_bq_client
    mock_bq_client.query.side_effect = Exception("Test exception")

    with pytest.raises(InternalServerError) as exc_info:
        moove_ai_query.query_moove_ai(moove_ai_query_data.coordinate_list)

    assert (
        str(exc_info.value)
        == "500 Internal Server Error: Encountered unknown issue querying Moove AI data: Test exception"
    )

    mock_bq_client.query.assert_called_once()
