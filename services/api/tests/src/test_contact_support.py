from unittest.mock import MagicMock

from mock import patch
import pytest

import api.src.contact_support as contact_support
import api.tests.data.contact_support_data as contact_support_data

DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS = "smtp.gmail.com"
DEFAULT_CSM_TARGET_SMTP_SERVER_PORT = 587


# tests for ContactSupportSchema class ---
def test_contact_support_schema():
    # prepare
    schema = contact_support.ContactSupportSchema()

    # execute
    exceptionOccurred = False
    try:
        schema.load(contact_support_data.contact_support_data)
    except Exception:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred is False


def test_contact_support_schema_invalid():
    # prepare
    schema = contact_support.ContactSupportSchema()

    # execute
    with pytest.raises(Exception):
        schema.load({})


# end of tests for ContactSupportSchema class ---


# tests for ContactSupportResource class ---
@patch(
    "api.src.api_environment.CSM_EMAIL_TO_SEND_FROM",
    contact_support_data.CSM_EMAIL_TO_SEND_FROM,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_USERNAME",
    contact_support_data.CSM_EMAIL_APP_USERNAME,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_PASSWORD",
    contact_support_data.CSM_EMAIL_APP_PASSWORD,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_PORT",
    DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.api_environment.CSM_TLS_ENABLED", True)
@patch("api.src.api_environment.CSM_AUTH_ENABLED", True)
def test_contact_support_resource_initialization_success():

    # execute
    contactSupportResource = contact_support.ContactSupportResource()

    # assert
    assert (
        contactSupportResource.CSM_EMAIL_TO_SEND_FROM
        == contact_support_data.CSM_EMAIL_TO_SEND_FROM
    )
    assert (
        contactSupportResource.CSM_EMAIL_APP_PASSWORD
        == contact_support_data.CSM_EMAIL_APP_PASSWORD
    )


@patch("api.src.api_environment.CSM_EMAIL_TO_SEND_FROM", None)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_USERNAME",
    contact_support_data.CSM_EMAIL_APP_USERNAME,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_PASSWORD",
    contact_support_data.CSM_EMAIL_APP_PASSWORD,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_PORT",
    DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.api_environment.CSM_TLS_ENABLED", True)
@patch("api.src.api_environment.CSM_AUTH_ENABLED", True)
def test_contact_support_resource_initialization_no_CSM_EMAIL_TO_SEND_FROM():
    # execute
    exceptionOccurred = False
    try:
        contact_support.ContactSupportResource()
    except Exception:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred

    # cleanup


@patch(
    "api.src.api_environment.CSM_EMAIL_TO_SEND_FROM",
    contact_support_data.CSM_EMAIL_TO_SEND_FROM,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_USERNAME",
    contact_support_data.CSM_EMAIL_APP_USERNAME,
)
@patch("api.src.api_environment.CSM_EMAIL_APP_PASSWORD", None)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_PORT",
    DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.api_environment.CSM_TLS_ENABLED", True)
@patch("api.src.api_environment.CSM_AUTH_ENABLED", True)
def test_contact_support_resource_initialization_no_CSM_EMAIL_APP_PASSWORD():
    # execute
    exceptionOccurred = False
    try:
        contact_support.ContactSupportResource()
    except Exception:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred


@patch(
    "api.src.api_environment.CSM_EMAIL_TO_SEND_FROM",
    contact_support_data.CSM_EMAIL_TO_SEND_FROM,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_USERNAME",
    contact_support_data.CSM_EMAIL_APP_USERNAME,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_PASSWORD",
    contact_support_data.CSM_EMAIL_APP_PASSWORD,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_PORT",
    DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.api_environment.CSM_TLS_ENABLED", True)
@patch("api.src.api_environment.CSM_AUTH_ENABLED", True)
def test_options():
    contactSupportResource = contact_support.ContactSupportResource()

    # execute
    result = contactSupportResource.options()

    # assert
    assert result == ("", 204, contactSupportResource.options_headers)


@patch(
    "api.src.api_environment.CSM_EMAIL_TO_SEND_FROM",
    contact_support_data.CSM_EMAIL_TO_SEND_FROM,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_USERNAME",
    contact_support_data.CSM_EMAIL_APP_USERNAME,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_PASSWORD",
    contact_support_data.CSM_EMAIL_APP_PASSWORD,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_PORT",
    DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.api_environment.CSM_TLS_ENABLED", True)
@patch("api.src.api_environment.CSM_AUTH_ENABLED", True)
def test_post_success():
    contactSupportResource = contact_support.ContactSupportResource()
    contactSupportResource.validate_input = MagicMock()
    contactSupportResource.send = MagicMock()
    contact_support.abort = MagicMock()
    contact_support.request = MagicMock()

    # execute
    result = contactSupportResource.post()

    # assert
    assert result == ("", 200, contactSupportResource.headers)


@patch(
    "api.src.api_environment.CSM_EMAIL_TO_SEND_FROM",
    contact_support_data.CSM_EMAIL_TO_SEND_FROM,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_USERNAME",
    contact_support_data.CSM_EMAIL_APP_USERNAME,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_PASSWORD",
    contact_support_data.CSM_EMAIL_APP_PASSWORD,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_PORT",
    DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.api_environment.CSM_TLS_ENABLED", True)
@patch("api.src.api_environment.CSM_AUTH_ENABLED", True)
def test_post_no_json_body():
    contactSupportResource = contact_support.ContactSupportResource()
    contactSupportResource.validate_input = MagicMock()
    contactSupportResource.send = MagicMock()
    contact_support.abort = MagicMock()
    contact_support.request = MagicMock()
    contact_support.request.json = None

    # execute
    result = contactSupportResource.post()

    # assert
    assert contact_support.abort.call_count == 2
    assert result == ("", 200, contactSupportResource.headers)


@patch(
    "api.src.api_environment.CSM_EMAIL_TO_SEND_FROM",
    contact_support_data.CSM_EMAIL_TO_SEND_FROM,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_USERNAME",
    contact_support_data.CSM_EMAIL_APP_USERNAME,
)
@patch(
    "api.src.api_environment.CSM_EMAIL_APP_PASSWORD",
    contact_support_data.CSM_EMAIL_APP_PASSWORD,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_ADDRESS",
    DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS,
)
@patch(
    "api.src.api_environment.CSM_TARGET_SMTP_SERVER_PORT",
    DEFAULT_CSM_TARGET_SMTP_SERVER_PORT,
)
@patch("api.src.api_environment.CSM_TLS_ENABLED", True)
@patch("api.src.api_environment.CSM_AUTH_ENABLED", True)
def test_validate_input():
    contactSupportResource = contact_support.ContactSupportResource()

    # execute
    result = contactSupportResource.validate_input(
        contact_support_data.contact_support_data
    )

    # assert
    assert result is None
