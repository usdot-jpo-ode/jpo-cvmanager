import os
from datetime import datetime, timedelta
from mock import MagicMock, patch
from addons.images.count_metric import daily_emailer


def test_query_mongo_in_counts():
    # prepare mocks and known variables
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_db.__getitem__.side_effect = mock_collection
    mock_collection().aggregate.return_value = [
        {"_id": ["10.0.0.1"], "count": 5},
        {"_id": ["10.0.0.2"], "count": 25},
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
    assert rsu_dict["10.0.0.2"]["counts"]["BSM"]["in"] == 25
    assert rsu_dict["10.0.0.2"]["primary_route"] == "Unknown"

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
                    "_id": f"$metadata.originIp",
                    "count": {"$sum": 1},
                }
            },
        ]
    )
    assert rsu_dict["10.0.0.1"]["counts"]["BSM"]["out"] == 5
    assert rsu_dict["10.0.0.2"]["counts"]["BSM"]["out"] == 25
    assert rsu_dict["10.0.0.2"]["primary_route"] == "Unknown"

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
def test_prepare_rsu_dict(mock_query_db):
    mock_query_db.return_value = [
        ({"ipv4_address": "10.0.0.1", "primary_route": "Route 1"},),
    ]
    daily_emailer.message_types = ["BSM"]

    # run
    result = daily_emailer.prepare_rsu_dict()

    expected_result = {
        "10.0.0.1": {"primary_route": "Route 1", "counts": {"BSM": {"in": 0, "out": 0}}}
    }
    mock_query_db.assert_called_once()
    assert result == expected_result

    daily_emailer.message_types = ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]


@patch.dict(
    os.environ,
    {
        "DEPLOYMENT_TITLE": "Test",
        "SMTP_SERVER_IP": "10.0.0.1",
        "SMTP_USERNAME": "username",
        "SMTP_PASSWORD": "password",
        "SMTP_EMAIL": "test@gmail.com",
        "SMTP_EMAIL_RECIPIENTS": "bob@gmail.com",
    },
)
@patch("addons.images.count_metric.daily_emailer.EmailSender")
def test_email_daily_counts(mock_emailsender):
    emailsender_obj = mock_emailsender.return_value

    daily_emailer.email_daily_counts("test")

    emailsender_obj.send.assert_called_once_with(
        sender="test@gmail.com",
        recipient="bob@gmail.com",
        subject="TEST Counts",
        message="test",
        replyEmail="",
        username="username",
        password="password",
        pretty=True,
    )


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "mongo-uri",
        "MONGO_DB_NAME": "test_db",
    },
)
@patch("addons.images.count_metric.daily_emailer.MongoClient", MagicMock())
@patch("addons.images.count_metric.daily_emailer.gen_email.generate_email_body")
@patch("addons.images.count_metric.daily_emailer.email_daily_counts")
@patch("addons.images.count_metric.daily_emailer.query_mongo_out_counts")
@patch("addons.images.count_metric.daily_emailer.query_mongo_in_counts")
@patch("addons.images.count_metric.daily_emailer.prepare_rsu_dict")
def test_run_daily_emailer(
    mock_prepare_rsu_dict,
    mock_query_mongo_in_counts,
    mock_query_mongo_out_counts,
    mock_email_daily_counts,
    mock_gen_email,
):
    daily_emailer.run_daily_emailer()

    mock_prepare_rsu_dict.assert_called_once()
    mock_query_mongo_in_counts.assert_called_once()
    mock_query_mongo_out_counts.assert_called_once()
    mock_email_daily_counts.assert_called_once()
    mock_gen_email.assert_called_once()
