import os
from unittest.mock import MagicMock

import api.src.rsu_error_summary as rsu_error_summary
import api.tests.data.rsu_error_summary_data as rsu_error_summary_data
from api.tests.data import auth_data
from common.auth_tools import ENVIRON_USER_KEY

user_valid = auth_data.get_request_environ()


# RSUErrorSummarySchema class tests ---
def test_rsu_error_summary_schema():
    # prepare
    schema = rsu_error_summary.RSUErrorSummarySchema()

    # execute
    exceptionOccurred = False
    try:
        schema.load(rsu_error_summary_data.rsu_error_summary_data_good)
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred == False


def test_rsu_error_summary_schema_invalid():
    # prepare
    schema = rsu_error_summary.RSUErrorSummarySchema()

    # execute
    exceptionOccurred = False
    try:
        schema.load(rsu_error_summary_data.rsu_error_summary_data_bad)
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred == True


# RSUErrorSummaryResource class tests ---
def test_options():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = rsu_error_summary_data.CSM_EMAIL_TO_SEND_FROM
    os.environ["CSM_EMAIL_APP_USERNAME"] = rsu_error_summary_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = rsu_error_summary_data.CSM_EMAIL_APP_PASSWORD
    os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"] = (
        rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    )
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(
        rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_PORT
    )
    RSUErrorSummaryResource = rsu_error_summary.RSUErrorSummaryResource()

    # execute
    result = RSUErrorSummaryResource.options()

    # assert
    assert result == ("", 204, RSUErrorSummaryResource.options_headers)

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]


def test_post_success():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = rsu_error_summary_data.CSM_EMAIL_TO_SEND_FROM
    os.environ["CSM_EMAIL_APP_USERNAME"] = rsu_error_summary_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = rsu_error_summary_data.CSM_EMAIL_APP_PASSWORD
    os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"] = (
        rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    )
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(
        rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_PORT
    )
    RSUErrorSummaryResource = rsu_error_summary.RSUErrorSummaryResource()
    RSUErrorSummaryResource.validate_input = MagicMock()
    RSUErrorSummaryResource.send = MagicMock()
    rsu_error_summary.abort = MagicMock()
    rsu_error_summary.request = MagicMock()

    # execute
    result = RSUErrorSummaryResource.post()

    # assert
    assert result == ("", 200, RSUErrorSummaryResource.headers)

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]


def test_post_no_json_body():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = rsu_error_summary_data.CSM_EMAIL_TO_SEND_FROM
    os.environ["CSM_EMAIL_APP_USERNAME"] = rsu_error_summary_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = rsu_error_summary_data.CSM_EMAIL_APP_PASSWORD
    os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"] = (
        rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    )
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(
        rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_PORT
    )
    RSUErrorSummaryResource = rsu_error_summary.RSUErrorSummaryResource()
    RSUErrorSummaryResource.validate_input = MagicMock()
    RSUErrorSummaryResource.send = MagicMock()
    rsu_error_summary.abort = MagicMock()
    rsu_error_summary.request = MagicMock()
    rsu_error_summary.request.json = None

    # execute
    result = RSUErrorSummaryResource.post()

    # assert
    assert rsu_error_summary.abort.call_count == 2
    assert result == ("", 200, RSUErrorSummaryResource.headers)

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]


def test_validate_input_good():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = rsu_error_summary_data.CSM_EMAIL_TO_SEND_FROM
    os.environ["CSM_EMAIL_APP_USERNAME"] = rsu_error_summary_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = rsu_error_summary_data.CSM_EMAIL_APP_PASSWORD
    os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"] = (
        rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    )
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(
        rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_PORT
    )
    RSUErrorSummaryResource = rsu_error_summary.RSUErrorSummaryResource()

    # execute
    result = RSUErrorSummaryResource.validate_input(
        rsu_error_summary_data.rsu_error_summary_data_good
    )

    # assert
    assert result == None

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]
