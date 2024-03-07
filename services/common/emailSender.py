import logging
import smtplib, ssl
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart


class EmailSender:
    def __init__(self, smtp_server, port):
        self.smtp_server = smtp_server
        self.port = port
        self.context = ssl._create_unverified_context()
        self.server = smtplib.SMTP(self.smtp_server, self.port)

    def send(
        self,
        sender,
        recipient,
        subject,
        message,
        replyEmail,
        username,
        password,
        pretty=False,
    ):
        try:
            # prepare email
            toSend = ""
            if pretty:
                toSend = self.preparePrettyEmailToSend(
                    sender, recipient, subject, message
                )
            else:
                toSend = self.prepareEmailToSend(
                    sender, recipient, subject, message, replyEmail
                )

            self.server.starttls(context=self.context)  # start TLS encryption
            self.server.ehlo()  # say hello
            self.server.login(username, password)

            # send email
            self.server.sendmail(sender, recipient, toSend)
            logging.debug(f"Email sent to {recipient}")
        except Exception as e:
            logging.error(e)
        finally:
            self.server.quit()

    def prepareEmailToSend(self, sender, recipient, subject, message, replyEmail):
        emailHeaders = "From: %s\r\nTo: %s\r\nSubject: %s\r\n\r\n" % (
            sender,
            recipient,
            subject,
        )
        if not replyEmail:
            toSend = emailHeaders + message
        else:
            toSend = emailHeaders + message + "\r\n\r\nReply-To: " + replyEmail
        return toSend

    def preparePrettyEmailToSend(self, sender, recipient, subject, html_message):
        toSend = MIMEMultipart()
        toSend["Subject"] = subject
        toSend["From"] = sender
        toSend["To"] = recipient
        toSend.attach(MIMEText(html_message, "html"))
        return toSend.as_string()
