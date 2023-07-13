import os
from unittest.mock import MagicMock

import api.src.contact_support as contact_support
import tests.data.contact_support_data as contact_support_data

DEFAULT_TARGET_SMTP_SERVER_ADDRESS = "smtp.gmail.com"
DEFAULT_TARGET_SMTP_SERVER_PORT = 587

# tests for ContactSupportSchema class ---

def test_contact_support_schema():
    # prepare
    schema = contact_support.ContactSupportSchema()

    # execute
    result = schema.load(contact_support_data.contact_support_data)

    # assert
    assert result == contact_support_data.contact_support_data

def test_contact_support_schema_invalid():
    # prepare
    schema = contact_support.ContactSupportSchema()

    # execute
    exceptionOccurred = False
    try:
        result = schema.load({})
    except Exception as e:
        exceptionOccurred = True
    
    # assert
    assert exceptionOccurred

# end of tests for ContactSupportSchema class ---

# tests for ContactSupportResource class ---

def test_contact_support_resource_initialization_success():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = contact_support_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = contact_support_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = contact_support_data.EMAILS_TO_SEND_TO
    os.environ['TARGET_SMTP_SERVER_ADDRESS'] = DEFAULT_TARGET_SMTP_SERVER_ADDRESS
    os.environ['TARGET_SMTP_SERVER_PORT'] = str(DEFAULT_TARGET_SMTP_SERVER_PORT)

    # execute
    contactSupportResource = contact_support.ContactSupportResource()

    # assert
    assert contactSupportResource.EMAIL_TO_SEND_FROM == contact_support_data.EMAIL_TO_SEND_FROM
    assert contactSupportResource.EMAIL_APP_PASSWORD == contact_support_data.EMAIL_APP_PASSWORD
    assert contactSupportResource.EMAILS_TO_SEND_TO == contact_support_data.EMAILS_TO_SEND_TO

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']
    del os.environ['TARGET_SMTP_SERVER_ADDRESS']
    del os.environ['TARGET_SMTP_SERVER_PORT']

def test_contact_support_resource_initialization_no_email_to_send_from():
    # prepare
    os.environ['EMAIL_APP_PASSWORD'] = contact_support_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = contact_support_data.EMAILS_TO_SEND_TO
    os.environ['TARGET_SMTP_SERVER_ADDRESS'] = DEFAULT_TARGET_SMTP_SERVER_ADDRESS
    os.environ['TARGET_SMTP_SERVER_PORT'] = str(DEFAULT_TARGET_SMTP_SERVER_PORT)

    # execute
    exceptionOccurred = False
    try:
        contactSupportResource = contact_support.ContactSupportResource()
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred

    # cleanup
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']
    del os.environ['TARGET_SMTP_SERVER_ADDRESS']
    del os.environ['TARGET_SMTP_SERVER_PORT']

def test_contact_support_resource_initialization_no_email_app_password():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = contact_support_data.EMAIL_TO_SEND_FROM
    os.environ['EMAILS_TO_SEND_TO'] = contact_support_data.EMAILS_TO_SEND_TO
    os.environ['TARGET_SMTP_SERVER_ADDRESS'] = DEFAULT_TARGET_SMTP_SERVER_ADDRESS
    os.environ['TARGET_SMTP_SERVER_PORT'] = str(DEFAULT_TARGET_SMTP_SERVER_PORT)

    # execute
    exceptionOccurred = False
    try:
        contactSupportResource = contact_support.ContactSupportResource()
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAILS_TO_SEND_TO']
    del os.environ['TARGET_SMTP_SERVER_ADDRESS']
    del os.environ['TARGET_SMTP_SERVER_PORT']

def test_contact_support_resource_initialization_no_emails_to_send_to():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = contact_support_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = contact_support_data.EMAIL_APP_PASSWORD
    os.environ['TARGET_SMTP_SERVER_ADDRESS'] = DEFAULT_TARGET_SMTP_SERVER_ADDRESS
    os.environ['TARGET_SMTP_SERVER_PORT'] = str(DEFAULT_TARGET_SMTP_SERVER_PORT)

    # execute
    exceptionOccurred = False
    try:
        contactSupportResource = contact_support.ContactSupportResource()
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['TARGET_SMTP_SERVER_ADDRESS']
    del os.environ['TARGET_SMTP_SERVER_PORT']

def test_options():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = contact_support_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = contact_support_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = contact_support_data.EMAILS_TO_SEND_TO
    os.environ['TARGET_SMTP_SERVER_ADDRESS'] = DEFAULT_TARGET_SMTP_SERVER_ADDRESS
    os.environ['TARGET_SMTP_SERVER_PORT'] = str(DEFAULT_TARGET_SMTP_SERVER_PORT)
    contactSupportResource = contact_support.ContactSupportResource()

    # execute
    result = contactSupportResource.options()

    # assert
    assert result == ('', 204, contactSupportResource.options_headers)

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']
    del os.environ['TARGET_SMTP_SERVER_ADDRESS']
    del os.environ['TARGET_SMTP_SERVER_PORT']

def test_post_success():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = contact_support_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = contact_support_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = contact_support_data.EMAILS_TO_SEND_TO
    os.environ['TARGET_SMTP_SERVER_ADDRESS'] = DEFAULT_TARGET_SMTP_SERVER_ADDRESS
    os.environ['TARGET_SMTP_SERVER_PORT'] = str(DEFAULT_TARGET_SMTP_SERVER_PORT)
    contactSupportResource = contact_support.ContactSupportResource()
    contactSupportResource.validate_input = MagicMock()
    contactSupportResource.send = MagicMock()
    contact_support.abort = MagicMock()
    contact_support.request = MagicMock()

    # execute
    result = contactSupportResource.post()

    # assert
    assert result == ('', 200, contactSupportResource.headers)

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']
    del os.environ['TARGET_SMTP_SERVER_ADDRESS']
    del os.environ['TARGET_SMTP_SERVER_PORT']

def test_post_no_json_body():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = contact_support_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = contact_support_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = contact_support_data.EMAILS_TO_SEND_TO
    os.environ['TARGET_SMTP_SERVER_ADDRESS'] = DEFAULT_TARGET_SMTP_SERVER_ADDRESS
    os.environ['TARGET_SMTP_SERVER_PORT'] = str(DEFAULT_TARGET_SMTP_SERVER_PORT)
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
    assert result == ('', 200, contactSupportResource.headers)

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']
    del os.environ['TARGET_SMTP_SERVER_ADDRESS']
    del os.environ['TARGET_SMTP_SERVER_PORT']

def test_validate_input():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = contact_support_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = contact_support_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = contact_support_data.EMAILS_TO_SEND_TO
    os.environ['TARGET_SMTP_SERVER_ADDRESS'] = DEFAULT_TARGET_SMTP_SERVER_ADDRESS
    os.environ['TARGET_SMTP_SERVER_PORT'] = str(DEFAULT_TARGET_SMTP_SERVER_PORT)
    contactSupportResource = contact_support.ContactSupportResource()

    # execute
    result = contactSupportResource.validate_input(contact_support_data.contact_support_data)

    # assert
    assert result == None

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']
    del os.environ['TARGET_SMTP_SERVER_ADDRESS']
    del os.environ['TARGET_SMTP_SERVER_PORT']

# end of tests for ContactSupportResource class ---