from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import logging, logging.handlers

from logging.handlers import SMTPHandler
import smtplib
import datetime
import ssl
import os

import pgquery


def get_subscribed_users():
  query = "SELECT email FROM public.users WHERE receive_error_emails = '1'"

  data = pgquery.query_db(query)

  return [point['email'] for point in data]


def configure_error_emails(app):
    
    mail_handler = SMTP_SSLHandler(mailhost=[os.getenv('CSM_TARGET_SMTP_SERVER_ADDRESS', '10.85.50.92'), int(os.getenv('CSM_TARGET_SMTP_SERVER_PORT', '587'))],
                                fromaddr=os.getenv('CSM_EMAIL_TO_SEND_FROM', 'rtdh@state.co.us'),
                                toaddrs=[],
                                subject='Automated CV Manager API Error',
                                credentials=[os.getenv('CSM_EMAIL_APP_USERNAME'), os.getenv('CSM_EMAIL_APP_PASSWORD')],
                                secure=())
    mail_handler.setLevel(logging.ERROR)
    mail_handler.setFormatter(logging.Formatter("")) # this seems weird, but it's the only way I can figure out how to include the stack trace info. This command appends the stack trace to the end of the self.format(record) call.
    app.logger.addHandler(mail_handler)
    

def get_environment_name(instance_connection_name):
    try:
        return instance_connection_name.split(':')[0]
    except:
        return str(instance_connection_name)


class SMTP_SSLHandler(SMTPHandler):
    def __init__(self, mailhost, fromaddr, toaddrs, subject, credentials=None, secure=None):
        super(SMTP_SSLHandler, self).__init__(mailhost, fromaddr, toaddrs, subject, credentials, secure)
            
    def emit(self, record):
        try:
            message = MIMEMultipart()
            message["Subject"] = self.subject
            message["From"] = self.fromaddr
            
            subscribed_users = get_subscribed_users()
            message["To"] = ','.join(subscribed_users)
        
            if not hasattr(record, 'asctime'):
                # For some reason, asctime is not always available. So we update it to the current time in the same format (2023-08-23 15:39:29,115)
                record.asctime = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S,%f")[:-3]
                
            body_content = open("./error_email/error_email_template.html").read()
            EMAIL_KEYS = {
                'ENVIRONMENT': get_environment_name(os.getenv('INSTANCE_CONNECTION_NAME', 'cdot-oim-cv-dev:us-west3:rsu-manager')),
                'ERROR_MESSAGE': self.format(record).replace("\n", "<br>"),
                'ERROR_TIME': str(record.asctime),
                'CLOUD_RUN_LOGS_LINK': os.getenv("CLOUD_RUN_LOGS_LINK", "https://console.cloud.google.com/run/detail/us-central1/rsu-manager-cloud-run-api/logs?authuser=1&project=cdot-oim-cv-dev"),
                'CONTACT_EMAIL': os.getenv("ERROR_EMAIL_CONTACT_EMAIL", 'jfrye@neaeraconsulting.com')
            }
            
            for key, value in EMAIL_KEYS.items():
                body_content = body_content.replace(f"##_{key}_##", value)
            message.attach(MIMEText(body_content, "html"))

            context = ssl._create_unverified_context()
            smtp = smtplib.SMTP(host=self.mailhost, port=self.mailport)
            smtp.starttls(context=context)
            smtp.ehlo()
            smtp.login(self.username, self.password)
            smtp.sendmail(self.fromaddr, subscribed_users, message.as_string())
            smtp.quit()
        
            logging.debug(f"Successfully sent error email to {subscribed_users}")
        except Exception as e:
            logging.exception(e)