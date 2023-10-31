import logging
import smtplib, ssl

class EmailSender():
    def __init__(self, smtp_server, port):
        self.smtp_server = smtp_server
        self.port = port
        self.context = ssl._create_unverified_context()
        self.server = smtplib.SMTP(self.smtp_server, self.port)
    
    def send(self, sender, recipient, subject, message, replyEmail, username, password):
        try:
            self.server.ehlo() # say hello to server
            self.server.starttls(context=self.context) # start TLS encryption
            self.server.ehlo() # say hello again
            self.server.login(username, password)

            # prepare email
            toSend = self.prepareEmailToSend(sender, recipient, subject, message, replyEmail)

            # send email
            self.server.sendmail(sender, recipient, toSend)
            logging.debug(f"Email sent to {recipient}")
        except Exception as e:
            print(e)
        finally:
            self.server.quit()
    
    def prepareEmailToSend(self, sender, recipient, subject, message, replyEmail):
        emailHeaders = "From: %s\r\nTo: %s\r\nSubject: %s\r\n\r\n" % (sender, recipient, subject)
        toSend = emailHeaders + message + "\r\n\r\nReply-To: " + replyEmail
        return toSend