import os
import threading
from unittest.mock import patch, MagicMock

from flask import app, copy_current_request_context

import api.src.send_email as send_email
import tests.data.send_email_data as send_email_data

# tests for SendEmailSchema class ---

def test_send_email_schema():
    # prepare
    schema = send_email.SendEmailSchema()

    # execute
    result = schema.load(send_email_data.send_email_data)

    # assert
    assert result == send_email_data.send_email_data

def test_send_email_schema_invalid():
    # prepare
    schema = send_email.SendEmailSchema()

    # execute
    exceptionOccurred = False
    try:
        result = schema.load({})
    except Exception as e:
        exceptionOccurred = True
    
    # assert
    assert exceptionOccurred

# end of tests for SendEmailSchema class ---

# tests for SendEmailResource class ---

def test_send_email_resource_initialization_success():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = send_email_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = send_email_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = send_email_data.EMAILS_TO_SEND_TO

    # execute
    sendEmailResource = send_email.SendEmailResource()

    # assert
    assert sendEmailResource.EMAIL_TO_SEND_FROM == send_email_data.EMAIL_TO_SEND_FROM
    assert sendEmailResource.EMAIL_APP_PASSWORD == send_email_data.EMAIL_APP_PASSWORD
    assert sendEmailResource.EMAILS_TO_SEND_TO == send_email_data.EMAILS_TO_SEND_TO

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']

def test_send_email_resource_initialization_no_email_to_send_from():
    # prepare
    os.environ['EMAIL_APP_PASSWORD'] = send_email_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = send_email_data.EMAILS_TO_SEND_TO

    # execute
    exceptionOccurred = False
    try:
        sendEmailResource = send_email.SendEmailResource()
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred

    # cleanup
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']

def test_send_email_resource_initialization_no_email_app_password():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = send_email_data.EMAIL_TO_SEND_FROM
    os.environ['EMAILS_TO_SEND_TO'] = send_email_data.EMAILS_TO_SEND_TO

    # execute
    exceptionOccurred = False
    try:
        sendEmailResource = send_email.SendEmailResource()
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAILS_TO_SEND_TO']

def test_send_email_resource_initialization_no_emails_to_send_to():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = send_email_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = send_email_data.EMAIL_APP_PASSWORD

    # execute
    exceptionOccurred = False
    try:
        sendEmailResource = send_email.SendEmailResource()
    except Exception as e:
        exceptionOccurred = True

    # assert
    assert exceptionOccurred

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']

def test_options():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = send_email_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = send_email_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = send_email_data.EMAILS_TO_SEND_TO
    sendEmailResource = send_email.SendEmailResource()

    # execute
    result = sendEmailResource.options()

    # assert
    assert result == ('', 204, sendEmailResource.options_headers)

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']

def test_post_success():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = send_email_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = send_email_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = send_email_data.EMAILS_TO_SEND_TO
    sendEmailResource = send_email.SendEmailResource()
    sendEmailResource.validate_input = MagicMock()
    sendEmailResource.send = MagicMock()
    send_email.abort = MagicMock()
    send_email.request = MagicMock()

    # execute
    result = sendEmailResource.post()

    # assert
    assert result == ('', 200, sendEmailResource.headers)

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']

def test_post_no_json_body():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = send_email_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = send_email_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = send_email_data.EMAILS_TO_SEND_TO
    sendEmailResource = send_email.SendEmailResource()
    sendEmailResource.validate_input = MagicMock()
    sendEmailResource.send = MagicMock()
    send_email.abort = MagicMock()
    send_email.request = MagicMock()
    send_email.request.json = None

    # execute
    result = sendEmailResource.post()

    # assert
    assert send_email.abort.call_count == 2
    assert result == ('', 200, sendEmailResource.headers)

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']

def test_validate_input():
    # prepare
    os.environ['EMAIL_TO_SEND_FROM'] = send_email_data.EMAIL_TO_SEND_FROM
    os.environ['EMAIL_APP_PASSWORD'] = send_email_data.EMAIL_APP_PASSWORD
    os.environ['EMAILS_TO_SEND_TO'] = send_email_data.EMAILS_TO_SEND_TO
    sendEmailResource = send_email.SendEmailResource()

    # execute
    result = sendEmailResource.validate_input(send_email_data.send_email_data)

    # assert
    assert result == None

    # cleanup
    del os.environ['EMAIL_TO_SEND_FROM']
    del os.environ['EMAIL_APP_PASSWORD']
    del os.environ['EMAILS_TO_SEND_TO']

# end of tests for SendEmailResource class ---

# tests for EmailSender class ---

def test_send():
    # prepare
    emailSender = send_email.EmailSender()
    emailSender.server = MagicMock()
    emailSender.server.starttls = MagicMock()
    emailSender.server.ehlo = MagicMock()
    emailSender.server.login = MagicMock()
    emailSender.server.sendmail = MagicMock()
    emailSender.server.quit = MagicMock()

    # execute
    emailSender.send(send_email_data.EMAIL_TO_SEND_FROM, send_email_data.EMAILS_TO_SEND_TO, send_email_data.EMAIL_SUBJECT, send_email_data.EMAIL_MESSAGE, send_email_data.EMAIL_REPLY_EMAIL, send_email_data.EMAIL_APP_PASSWORD)

    # assert
    emailSender.server.starttls.assert_called_once()
    assert emailSender.server.ehlo.call_count == 2
    emailSender.server.login.assert_called_once()
    emailSender.server.sendmail.assert_called_once()
    emailSender.server.quit.assert_called_once()

# end of tests for EmailSender class ---