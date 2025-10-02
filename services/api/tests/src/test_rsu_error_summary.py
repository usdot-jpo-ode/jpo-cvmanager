from unittest.mock import MagicMock

from mock import patch

import api.src.rsu_error_summary as rsu_error_summary
import api.tests.data.rsu_error_summary_data as rsu_error_summary_data

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
    assert exceptionOccurred is False


def test_rsu_error_summary_schema_invalid():
    # prepare
    schema = rsu_error_summary.RSUErrorSummarySchema()

    # execute
    exceptionOccurred = False
    try:
        schema.load(rsu_error_summary_data.rsu_error_summary_data_bada)
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred is True


# RSUErrorSummaryResource class tests ---
@patch(
    "api.src.environment.CSM_EMAIL_TO_SEND_FROM",
    rsu_error_summary_data.CSM_EMAIL_TO_SEND_FROM,
)
@patch(
    "api.src.environment.CSM_EMAIL_APP_USERNAME",
    rsu_error_summary_data.CSM_EMAIL_APP_USERNAME,
)
@patch(
    "api.src.environment.CSM_EMAIL_APP_PASSWORD",
    rsu_error_summary_data.CSM_EMAIL_APP_PASSWORD,
)
@patch(
    "api.src.environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.environment.CSM_TARGET_SMTP_SERVER_PORT",
    rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.environment.CSM_TLS_ENABLED", True)
@patch("api.src.environment.CSM_AUTH_ENABLED", True)
def test_options():
    RSUErrorSummaryResource = rsu_error_summary.RSUErrorSummaryResource()

    # execute
    result = RSUErrorSummaryResource.options()

    # assert
    assert result == ("", 204, RSUErrorSummaryResource.options_headers)


@patch(
    "api.src.environment.CSM_EMAIL_TO_SEND_FROM",
    rsu_error_summary_data.CSM_EMAIL_TO_SEND_FROM,
)
@patch(
    "api.src.environment.CSM_EMAIL_APP_USERNAME",
    rsu_error_summary_data.CSM_EMAIL_APP_USERNAME,
)
@patch(
    "api.src.environment.CSM_EMAIL_APP_PASSWORD",
    rsu_error_summary_data.CSM_EMAIL_APP_PASSWORD,
)
@patch(
    "api.src.environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.environment.CSM_TARGET_SMTP_SERVER_PORT",
    rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.environment.CSM_TLS_ENABLED", True)
@patch("api.src.environment.CSM_AUTH_ENABLED", True)
def test_post_success():
    RSUErrorSummaryResource = rsu_error_summary.RSUErrorSummaryResource()
    RSUErrorSummaryResource.validate_input = MagicMock()
    RSUErrorSummaryResource.send = MagicMock()
    rsu_error_summary.abort = MagicMock()
    rsu_error_summary.request = MagicMock()

    # execute
    result = RSUErrorSummaryResource.post()

    # assert
    assert result == ("", 200, RSUErrorSummaryResource.headers)


@patch(
    "api.src.environment.CSM_EMAIL_TO_SEND_FROM",
    rsu_error_summary_data.CSM_EMAIL_TO_SEND_FROM,
)
@patch(
    "api.src.environment.CSM_EMAIL_APP_USERNAME",
    rsu_error_summary_data.CSM_EMAIL_APP_USERNAME,
)
@patch(
    "api.src.environment.CSM_EMAIL_APP_PASSWORD",
    rsu_error_summary_data.CSM_EMAIL_APP_PASSWORD,
)
@patch(
    "api.src.environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.environment.CSM_TARGET_SMTP_SERVER_PORT",
    rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.environment.CSM_TLS_ENABLED", True)
@patch("api.src.environment.CSM_AUTH_ENABLED", True)
def test_post_no_json_body():
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


@patch(
    "api.src.environment.CSM_EMAIL_TO_SEND_FROM",
    rsu_error_summary_data.CSM_EMAIL_TO_SEND_FROM,
)
@patch(
    "api.src.environment.CSM_EMAIL_APP_USERNAME",
    rsu_error_summary_data.CSM_EMAIL_APP_USERNAME,
)
@patch(
    "api.src.environment.CSM_EMAIL_APP_PASSWORD",
    rsu_error_summary_data.CSM_EMAIL_APP_PASSWORD,
)
@patch(
    "api.src.environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.environment.CSM_TARGET_SMTP_SERVER_PORT",
    rsu_error_summary_data.DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.environment.CSM_TLS_ENABLED", True)
@patch("api.src.environment.CSM_AUTH_ENABLED", True)
def test_validate_input_good():
    RSUErrorSummaryResource = rsu_error_summary.RSUErrorSummaryResource()

    # execute
    result = RSUErrorSummaryResource.validate_input(
        rsu_error_summary_data.rsu_error_summary_data_good
    )

    # assert
    assert result is None
