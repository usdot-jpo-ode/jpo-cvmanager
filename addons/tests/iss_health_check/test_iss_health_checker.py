from unittest.mock import patch, MagicMock, Mock
import os

from images.iss_health_check import iss_health_checker

@patch('images.iss_health_check.iss_health_checker.pgquery.query_db')
def test_get_rsu_data_no_data(mock_query_db):
  mock_query_db.return_value = []
  result = iss_health_checker.get_rsu_data()

  # check
  assert result == {}
  mock_query_db.assert_called_once()
  mock_query_db.assert_called_with("SELECT jsonb_build_object('rsu_id', rsu_id, 'iss_scms_id', iss_scms_id) FROM public.rsus WHERE iss_scms_id IS NOT NULL ORDER BY rsu_id")

@patch('images.iss_health_check.iss_health_checker.pgquery.query_db')
def test_get_rsu_data_with_data(mock_query_db):
  # mock
  mock_query_db.return_value = [
      [{'rsu_id': 1, 'iss_scms_id': 'ABC'}],
      [{'rsu_id': 2, 'iss_scms_id': 'DEF'}],
      [{'rsu_id': 3, 'iss_scms_id': 'GHI'}]
    ]
  result = iss_health_checker.get_rsu_data()
  
  expected_result = {
    "ABC": {"rsu_id": 1},
    "DEF": {"rsu_id": 2},
    "GHI": {"rsu_id": 3}
  }

  # check
  assert result == expected_result
  mock_query_db.assert_called_once()
  mock_query_db.assert_called_with("SELECT jsonb_build_object('rsu_id', rsu_id, 'iss_scms_id', iss_scms_id) FROM public.rsus WHERE iss_scms_id IS NOT NULL ORDER BY rsu_id")

@patch.dict(os.environ, {
  "ISS_API_KEY": "test",
  "ISS_SCMS_VEHICLE_REST_ENDPOINT": "https://api.dm.iss-scms.com/api/test",
  "ISS_PROJECT_ID": "test"
})
@patch('images.iss_health_check.iss_health_checker.requests.Response')
@patch('images.iss_health_check.iss_health_checker.requests')
@patch('images.iss_health_check.iss_health_checker.iss_token')
@patch('images.iss_health_check.iss_health_checker.get_rsu_data')
def test_get_scms_status_data(mock_get_rsu_data, mock_get_token, mock_requests, mock_response):
  mock_get_rsu_data.return_value = {
    "ABC": {"rsu_id": 1},
    "DEF": {"rsu_id": 2}
  }
  mock_get_token.get_token.return_value = "test-token"
  mock_requests.get.return_value = mock_response
  mock_response.json.side_effect = [
      {
        "data": [
          {
            "_id": "ABC",
            "provisionerCompany_id": "company",
            "entityType": "rsu",
            "project_id": "test",
            "deviceHealth": "Healthy",
            "enrollments": [
              {
                "enrollmentStartTime": "2022-10-02T00:00:00.000Z",
                "authorizationCertInfo": {
                  "expireTimeOfLatestDownloadedCert": "2022-11-02T00:00:00.000Z"
                }
              }
            ]
          },
          {
            "_id": "DEF",
            "provisionerCompany_id": "company",
            "entityType": "rsu",
            "project_id": "test",
            "deviceHealth": "Unhealthy",
            "enrollments": [
              {
                "enrollmentStartTime": "2022-10-02T00:00:00.000Z"
              }
            ]
          }
        ],
        "count": 2
      }, 
      {
        "data": [],
        "count": 2
      }
    ]
  
  actual_result = iss_health_checker.get_scms_status_data()
  
  expected_result = {
          "ABC": {
                  "rsu_id": 1,
                  "provisionerCompany": "company",
                  "entityType": "rsu",
                  "project_id": "test",
                  "deviceHealth": "Healthy",
                  "expiration": "2022-11-02T00:00:00.000Z"
              },
          "DEF": {
                  "rsu_id": 2,
                  "provisionerCompany": "company",
                  "entityType": "rsu",
                  "project_id": "test",
                  "deviceHealth": "Unhealthy",
                  "expiration": None
              }
      }
  

  assert actual_result == expected_result
  mock_get_rsu_data.assert_called_with()
  mock_get_token.get_token.assert_called_with()
  # Assert what should be the last call on the endpoint
  mock_requests.get.assert_called_with("https://api.dm.iss-scms.com/api/test?pageSize=200&page=1&project_id=test", headers={'x-api-key': 'test-token'})

@patch('images.iss_health_check.iss_health_checker.datetime')
@patch('images.iss_health_check.iss_health_checker.pgquery.query_db')
def test_insert_scms_data(mock_query_db, mock_datetime):
  mock_datetime.strftime.return_value = "2022-11-03T00:00:00.000Z"
  test_data = {
    "ABC": {
      "rsu_id": 1,
      "deviceHealth": "Healthy",
      "expiration": "2022-11-02T00:00:00.000Z"
      },
    "DEF": {
      "rsu_id": 2,
      "deviceHealth": "Unhealthy",
      "expiration": None
      },
  }
  # call
  iss_health_checker.insert_scms_data(test_data)

  expectedQuery = "INSERT INTO public.scms_health(\"timestamp\", health, expiration, rsu_id) VALUES " \
    "('2022-11-03T00:00:00.000Z', '1', '2022-11-02T00:00:00.000Z', 1), " \
    "('2022-11-03T00:00:00.000Z', '0', NULL, 2)"
  mock_query_db.assert_called_with(expectedQuery, no_return=True)
