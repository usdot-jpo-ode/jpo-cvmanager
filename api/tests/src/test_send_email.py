import os
from unittest.mock import patch, MagicMock

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

# def test_send_email_resource_initialization():
#     # TODO: implement
#     pass

# def test_options():
#     # TODO: implement
#     pass

# def test_post():
#     # TODO: implement
#     pass

# def test_validate_input():
#     # TODO: implement
#     pass

# end of tests for SendEmailResource class ---

# tests for EmailSender class ---

# def test_email_sender_initialization():
#     # TODO: implement
#     pass

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
    emailSender.send(send_email_data.EMAIL_TO_SEND_FROM, send_email_data.EMAIL_TO_SEND_TO, send_email_data.EMAIL_SUBJECT, send_email_data.EMAIL_MESSAGE, send_email_data.EMAIL_REPLY_EMAIL, send_email_data.EMAIL_APP_PASSWORD)

    # assert
    emailSender.server.starttls.assert_called_once()
    assert emailSender.server.ehlo.call_count == 2
    emailSender.server.login.assert_called_once()
    emailSender.server.sendmail.assert_called_once()
    emailSender.server.quit.assert_called_once()

# def test_send_exception():
#     # TODO: implement
#     pass

# end of tests for EmailSender class ---