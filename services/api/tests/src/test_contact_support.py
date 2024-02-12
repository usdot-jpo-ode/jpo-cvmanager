import os
from unittest.mock import MagicMock

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
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred == False


def test_contact_support_schema_invalid():
    # prepare
    schema = contact_support.ContactSupportSchema()

    # execute
    with pytest.raises(Exception):
        schema.load({})


# end of tests for ContactSupportSchema class ---

# tests for ContactSupportResource class ---


def test_contact_support_resource_initialization_success():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = contact_support_data.CSM_EMAIL_TO_SEND_FROM
    os.environ["CSM_EMAILS_TO_SEND_TO"] = contact_support_data.CSM_EMAILS_TO_SEND_TO
    os.environ[
        "CSM_TARGET_SMTP_SERVER_ADDRESS"
    ] = DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(DEFAULT_CSM_TARGET_SMTP_SERVER_PORT)
    os.environ["CSM_TLS_ENABLED"] = "true"
    os.environ["CSM_AUTH_ENABLED"] = "true"
    os.environ["CSM_EMAIL_APP_USERNAME"] = contact_support_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = contact_support_data.CSM_EMAIL_APP_PASSWORD

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
    assert (
        contactSupportResource.CSM_EMAILS_TO_SEND_TO
        == contact_support_data.CSM_EMAILS_TO_SEND_TO
    )

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_EMAILS_TO_SEND_TO"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]
    del os.environ["CSM_TLS_ENABLED"]
    del os.environ["CSM_AUTH_ENABLED"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]


def test_contact_support_resource_initialization_no_CSM_EMAIL_TO_SEND_FROM():
    # prepare
    os.environ["CSM_EMAILS_TO_SEND_TO"] = contact_support_data.CSM_EMAILS_TO_SEND_TO
    os.environ[
        "CSM_TARGET_SMTP_SERVER_ADDRESS"
    ] = DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(DEFAULT_CSM_TARGET_SMTP_SERVER_PORT)
    os.environ["CSM_TLS_ENABLED"] = "true"
    os.environ["CSM_AUTH_ENABLED"] = "true"
    os.environ["CSM_EMAIL_APP_USERNAME"] = contact_support_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = contact_support_data.CSM_EMAIL_APP_PASSWORD

    # execute
    exceptionOccurred = False
    try:
        contactSupportResource = contact_support.ContactSupportResource()
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred

    # cleanup
    del os.environ["CSM_EMAILS_TO_SEND_TO"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]
    del os.environ["CSM_TLS_ENABLED"]
    del os.environ["CSM_AUTH_ENABLED"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]


def test_contact_support_resource_initialization_no_CSM_EMAIL_APP_PASSWORD():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = contact_support_data.CSM_EMAIL_TO_SEND_FROM
    os.environ["CSM_EMAILS_TO_SEND_TO"] = contact_support_data.CSM_EMAILS_TO_SEND_TO
    os.environ[
        "CSM_TARGET_SMTP_SERVER_ADDRESS"
    ] = DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(DEFAULT_CSM_TARGET_SMTP_SERVER_PORT)
    os.environ["CSM_TLS_ENABLED"] = "true"
    os.environ["CSM_AUTH_ENABLED"] = "true"
    os.environ["CSM_EMAIL_APP_USERNAME"] = contact_support_data.CSM_EMAIL_APP_USERNAME

    # execute
    exceptionOccurred = False
    try:
        contactSupportResource = contact_support.ContactSupportResource()
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_EMAILS_TO_SEND_TO"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]
    del os.environ["CSM_TLS_ENABLED"]
    del os.environ["CSM_AUTH_ENABLED"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]


def test_contact_support_resource_initialization_no_CSM_EMAILS_TO_SEND_TO():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = contact_support_data.CSM_EMAIL_TO_SEND_FROM
    os.environ[
        "CSM_TARGET_SMTP_SERVER_ADDRESS"
    ] = DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(DEFAULT_CSM_TARGET_SMTP_SERVER_PORT)
    os.environ["CSM_TLS_ENABLED"] = "true"
    os.environ["CSM_AUTH_ENABLED"] = "true"
    os.environ["CSM_EMAIL_APP_USERNAME"] = contact_support_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = contact_support_data.CSM_EMAIL_APP_PASSWORD

    # execute
    exceptionOccurred = False
    try:
        contactSupportResource = contact_support.ContactSupportResource()
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]
    del os.environ["CSM_TLS_ENABLED"]
    del os.environ["CSM_AUTH_ENABLED"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]


def test_options():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = contact_support_data.CSM_EMAIL_TO_SEND_FROM
    os.environ["CSM_EMAILS_TO_SEND_TO"] = contact_support_data.CSM_EMAILS_TO_SEND_TO
    os.environ[
        "CSM_TARGET_SMTP_SERVER_ADDRESS"
    ] = DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(DEFAULT_CSM_TARGET_SMTP_SERVER_PORT)
    os.environ["CSM_TLS_ENABLED"] = "true"
    os.environ["CSM_AUTH_ENABLED"] = "true"
    os.environ["CSM_EMAIL_APP_USERNAME"] = contact_support_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = contact_support_data.CSM_EMAIL_APP_PASSWORD
    contactSupportResource = contact_support.ContactSupportResource()

    # execute
    result = contactSupportResource.options()

    # assert
    assert result == ("", 204, contactSupportResource.options_headers)

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_EMAILS_TO_SEND_TO"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]
    del os.environ["CSM_TLS_ENABLED"]
    del os.environ["CSM_AUTH_ENABLED"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]


def test_post_success():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = contact_support_data.CSM_EMAIL_TO_SEND_FROM
    os.environ["CSM_EMAILS_TO_SEND_TO"] = contact_support_data.CSM_EMAILS_TO_SEND_TO
    os.environ[
        "CSM_TARGET_SMTP_SERVER_ADDRESS"
    ] = DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(DEFAULT_CSM_TARGET_SMTP_SERVER_PORT)
    os.environ["CSM_TLS_ENABLED"] = "true"
    os.environ["CSM_AUTH_ENABLED"] = "true"
    os.environ["CSM_EMAIL_APP_USERNAME"] = contact_support_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = contact_support_data.CSM_EMAIL_APP_PASSWORD
    contactSupportResource = contact_support.ContactSupportResource()
    contactSupportResource.validate_input = MagicMock()
    contactSupportResource.send = MagicMock()
    contact_support.abort = MagicMock()
    contact_support.request = MagicMock()

    # execute
    result = contactSupportResource.post()

    # assert
    assert result == ("", 200, contactSupportResource.headers)

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_EMAILS_TO_SEND_TO"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]
    del os.environ["CSM_TLS_ENABLED"]
    del os.environ["CSM_AUTH_ENABLED"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]


def test_post_no_json_body():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = contact_support_data.CSM_EMAIL_TO_SEND_FROM
    os.environ["CSM_EMAILS_TO_SEND_TO"] = contact_support_data.CSM_EMAILS_TO_SEND_TO
    os.environ[
        "CSM_TARGET_SMTP_SERVER_ADDRESS"
    ] = DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(DEFAULT_CSM_TARGET_SMTP_SERVER_PORT)
    os.environ["CSM_TLS_ENABLED"] = "true"
    os.environ["CSM_AUTH_ENABLED"] = "true"
    os.environ["CSM_EMAIL_APP_USERNAME"] = contact_support_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = contact_support_data.CSM_EMAIL_APP_PASSWORD
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

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_EMAILS_TO_SEND_TO"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]
    del os.environ["CSM_TLS_ENABLED"]
    del os.environ["CSM_AUTH_ENABLED"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]


def test_validate_input():
    # prepare
    os.environ["CSM_EMAIL_TO_SEND_FROM"] = contact_support_data.CSM_EMAIL_TO_SEND_FROM
    os.environ["CSM_EMAILS_TO_SEND_TO"] = contact_support_data.CSM_EMAILS_TO_SEND_TO
    os.environ[
        "CSM_TARGET_SMTP_SERVER_ADDRESS"
    ] = DEFAULT_CSM_TARGET_SMTP_SERVER_ADDRESS
    os.environ["CSM_TARGET_SMTP_SERVER_PORT"] = str(DEFAULT_CSM_TARGET_SMTP_SERVER_PORT)
    os.environ["CSM_TLS_ENABLED"] = "true"
    os.environ["CSM_AUTH_ENABLED"] = "true"
    os.environ["CSM_EMAIL_APP_USERNAME"] = contact_support_data.CSM_EMAIL_APP_USERNAME
    os.environ["CSM_EMAIL_APP_PASSWORD"] = contact_support_data.CSM_EMAIL_APP_PASSWORD
    contactSupportResource = contact_support.ContactSupportResource()

    # execute
    result = contactSupportResource.validate_input(
        contact_support_data.contact_support_data
    )

    # assert
    assert result == None

    # cleanup
    del os.environ["CSM_EMAIL_TO_SEND_FROM"]
    del os.environ["CSM_EMAILS_TO_SEND_TO"]
    del os.environ["CSM_TARGET_SMTP_SERVER_ADDRESS"]
    del os.environ["CSM_TARGET_SMTP_SERVER_PORT"]
    del os.environ["CSM_TLS_ENABLED"]
    del os.environ["CSM_AUTH_ENABLED"]
    del os.environ["CSM_EMAIL_APP_USERNAME"]
    del os.environ["CSM_EMAIL_APP_PASSWORD"]

# end of tests for ContactSupportResource class ---
