from datetime import datetime, timedelta
from mock import MagicMock, patch
from addons.images.count_metric import daily_emailer


def test_query_mongo_in_counts():
    # prepare mocks and known variables
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_db.__getitem__.side_effect = mock_collection
    mock_collection().aggregate.return_value = [
        {"_id": "10.0.0.1", "count": 5},
        {"_id": "10.0.0.2", "count": 25},
    ]

    start_dt = (datetime.now() - timedelta(1)).replace(
        hour=0, minute=0, second=0, microsecond=0
    )
    end_dt = (datetime.now()).replace(hour=0, minute=0, second=0, microsecond=0)

    rsu_dict = {
        "10.0.0.1": {
            "primary_route": "Route 1",
            "counts": {"BSM": {"in": 0, "out": 0}},
        }
    }

    daily_emailer.message_types = ["BSM"]

    # run the command
    daily_emailer.query_mongo_in_counts(rsu_dict, start_dt, end_dt, mock_db)

    # make assertions
    mock_collection().aggregate.assert_called_once_with(
        [
            {
                "$match": {
                    "recordGeneratedAt": {
                        "$gte": start_dt,
                        "$lt": end_dt,
                    }
                }
            },
            {
                "$group": {
                    "_id": "$metadata.originIp",
                    "count": {"$sum": 1},
                }
            },
        ]
    )
    assert rsu_dict["10.0.0.1"]["counts"]["BSM"]["in"] == 5
    assert len(rsu_dict) == 1

    daily_emailer.message_types = ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]


def test_query_mongo_in_counts_no_id():
    # prepare mocks and known variables
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_db.__getitem__.side_effect = mock_collection
    mock_collection().aggregate.return_value = [{"_id": None, "count": 5}]

    start_dt = (datetime.now() - timedelta(1)).replace(
        hour=0, minute=0, second=0, microsecond=0
    )
    end_dt = (datetime.now()).replace(hour=0, minute=0, second=0, microsecond=0)

    rsu_dict = {
        "10.0.0.1": {
            "primary_route": "Route 1",
            "counts": {"BSM": {"in": 0, "out": 0}},
        }
    }

    daily_emailer.message_types = ["BSM"]

    # run the command
    daily_emailer.query_mongo_in_counts(rsu_dict, start_dt, end_dt, mock_db)

    # make assertions
    assert rsu_dict["10.0.0.1"]["counts"]["BSM"]["in"] == 0

    daily_emailer.message_types = ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]


def test_query_mongo_out_counts():
    # prepare mocks and known variables
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_db.__getitem__.side_effect = mock_collection
    mock_collection().aggregate.return_value = [
        {"_id": "10.0.0.1", "count": 5},
        {"_id": "10.0.0.2", "count": 25},
    ]

    start_dt = (datetime.now() - timedelta(1)).replace(
        hour=0, minute=0, second=0, microsecond=0
    )
    end_dt = (datetime.now()).replace(hour=0, minute=0, second=0, microsecond=0)

    rsu_dict = {
        "10.0.0.1": {
            "primary_route": "Route 1",
            "counts": {"BSM": {"in": 0, "out": 0}},
        }
    }

    daily_emailer.message_types = ["BSM"]

    # run the command
    daily_emailer.query_mongo_out_counts(rsu_dict, start_dt, end_dt, mock_db)

    # make assertions
    mock_collection().aggregate.assert_called_once_with(
        [
            {
                "$match": {
                    "recordGeneratedAt": {
                        "$gte": start_dt,
                        "$lt": end_dt,
                    }
                }
            },
            {
                "$group": {
                    "_id": "$metadata.originIp",
                    "count": {"$sum": 1},
                }
            },
        ]
    )
    assert rsu_dict["10.0.0.1"]["counts"]["BSM"]["out"] == 5
    assert len(rsu_dict) == 1

    daily_emailer.message_types = ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]


def test_query_mongo_out_counts_no_id():
    # prepare mocks and known variables
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_db.__getitem__.side_effect = mock_collection
    mock_collection().aggregate.return_value = [{"_id": None, "count": 5}]

    start_dt = (datetime.now() - timedelta(1)).replace(
        hour=0, minute=0, second=0, microsecond=0
    )
    end_dt = (datetime.now()).replace(hour=0, minute=0, second=0, microsecond=0)

    rsu_dict = {
        "10.0.0.1": {
            "primary_route": "Route 1",
            "counts": {"BSM": {"in": 0, "out": 0}},
        }
    }

    daily_emailer.message_types = ["BSM"]

    # run the command
    daily_emailer.query_mongo_out_counts(rsu_dict, start_dt, end_dt, mock_db)

    # make assertions
    assert rsu_dict["10.0.0.1"]["counts"]["BSM"]["out"] == 0

    daily_emailer.message_types = ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]


@patch("addons.images.count_metric.daily_emailer.pgquery.query_db")
def test_prepare_org_rsu_dict(mock_query_db):
    mock_query_db.return_value = [
        (
            {
                "org_name": "Test Org",
                "ipv4_address": "10.0.0.1",
                "primary_route": "Route 1",
            },
        ),
    ]
    daily_emailer.message_types = ["BSM"]

    # run
    result = daily_emailer.prepare_org_rsu_dict()

    expected_result = {
        "Test Org": {
            "10.0.0.1": {
                "primary_route": "Route 1",
                "counts": {"BSM": {"in": 0, "out": 0}},
            }
        }
    }
    mock_query_db.assert_called_once()
    assert result == expected_result

    daily_emailer.message_types = ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]


@patch("count_metric_environment.IAPI_ENDPOINT", "http://test.test")
@patch("count_metric_environment.KC_SA_CLIENT_ID", "sa_client_id")
@patch("count_metric_environment.KC_SA_CLIENT_SECRET", "sa_client_secret")
@patch("addons.images.count_metric.daily_emailer.EmailApi")
@patch("addons.images.count_metric.daily_emailer.KeycloakServiceAccountApi")
def test_email_daily_counts(mock_kc_api, mock_email_api):
    email_api_obj = mock_email_api.return_value

    org_name = "Test Org"
    deployment_title = "Test Deployment"
    start_date = datetime(2023, 1, 1, 0, 0, 0)
    end_date = datetime(2023, 1, 2, 0, 0, 0)
    message_type_list = ["BSM", "TIM"]
    counts = [
        {
            "rsu_ip": "10.0.0.1",
            "counts": {"BSM": {"in": 10, "out": 5}},
            "primary_route": "Route 1",
        }
    ]

    daily_emailer.email_daily_counts(
        org_name, deployment_title, start_date, end_date, message_type_list, counts
    )

    mock_email_api.assert_called_once_with(
        iapi_base_url="http://test.test", kc_api=mock_kc_api()
    )
    email_api_obj.send_message_counts.assert_called_once_with(
        org_name, deployment_title, start_date, end_date, message_type_list, counts
    )


@patch("count_metric_environment.DEPLOYMENT_TITLE", "Test Deployment")
@patch("count_metric_environment.MONGO_DB_URI", "mongo-uri")
@patch("count_metric_environment.MONGO_DB_NAME", "test_db")
@patch("addons.images.count_metric.daily_emailer.MongoClient", MagicMock())
@patch("addons.images.count_metric.daily_emailer.email_daily_counts")
@patch("addons.images.count_metric.daily_emailer.query_mongo_out_counts")
@patch("addons.images.count_metric.daily_emailer.query_mongo_in_counts")
@patch("addons.images.count_metric.daily_emailer.prepare_org_rsu_dict")
def test_run_daily_emailer(
    mock_prepare_org_rsu_dict,
    mock_query_mongo_in_counts,
    mock_query_mongo_out_counts,
    mock_email_daily_counts,
):
    mock_prepare_org_rsu_dict.return_value = {
        "Test Org": {
            "10.0.0.1": {
                "primary_route": "Route 1",
                "counts": {"BSM": {"in": 10, "out": 5}},
            }
        }
    }

    daily_emailer.run_daily_emailer()

    mock_prepare_org_rsu_dict.assert_called_once()
    mock_query_mongo_in_counts.assert_called_once()
    mock_query_mongo_out_counts.assert_called_once()
    mock_email_daily_counts.assert_called_once()
