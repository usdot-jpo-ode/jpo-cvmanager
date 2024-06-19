# Count Metric Service

## Daily Emailer (MongoDB)

The count_metric service provides a means of querying and processing the jpo-ode messages stored in mongoDB from your jpo-ode deployment environment. These processed messages are quantified and compiled into a message count summary email that includes a breakdown of all messages received for the past 24 hours. These counts are grouped by RSU that forwarded the data. Both "in" and "out" counts are collected to be able to determine if there has been any data loss during the processing within the jpo-ode. Any deviance greater than 5% will have its 'out' counts marked red in the generated email. Anything below 5% will be marked green.

It is important to note that the count_metric service assumes Map and TIM messages are deduplicated on the 'out' counts. It will normalize the deviance expectation to 1 unique Map or TIM per hour from a RSU.

Specifically includes the following message types: ["BSM", "TIM", "Map", "SPaT", "SRM", "SSM"]

To run this service, the following environment variables must be set:

<b>LOGGING_LEVEL:</b> The logging level of the deployment. Options are: 'critical', 'error', 'warning', 'info' and 'debug'. If not specified, will default to 'info'. Refer to Python's documentation for more info: [Python logging](https://docs.python.org/3/howto/logging.html).

<b>ENABLE_EMAILER:</b> Set to 'True' to run the daily emailer or 'False' to use the now deprecated Kafka message counter. It is recommended to switch to mongoDB if you are still using the message counter in any environments.

<b>DEPLOYMENT_TITLE:</b> The name of the environment that the jpo-ode messages are relevant to. This can be 'DEV', 'PROD', or anything suitable to your jpo-ode deployment.

<b>PG_DB_HOST:</b> The connection information for the Postgres database.

<b>PG_DB_USER:</b> Postgres database username.

<b>PG_DB_PASS:</b> Postgres database password, surround in single quotes if this has any special characters.

<b>PG_DB_NAME:</b> Postgres database name.

<b>MONGO_DB_URI:</b> Connection string uri for the MongoDB database, please refer to the following [documentation](https://www.mongodb.com/docs/manual/reference/connection-string/).

<b>MONGO_DB_NAME:</b> MongoDB database name.

<b>SMTP_SERVER_IP:</b> The IP or domain of the SMTP server your organization uses. DOTs often have a self hosted SMTP server for security reasons.

<b>SMTP_USERNAME:</b> The username for the SMTP server account.

<b>SMTP_PASSWORD:</b> The password for the SMTP server account.

<b>SMTP_EMAIL:</b> The origin email that the count_metric will send the email from. This is usually associated with the SMTP server authentication.

## Daily Counter (MongoDB)

The daily counter is a feature that aggregates JPO-ODE mongoDB message type counts for BSM, PSM, TIM, Map, SPaT, SRM and SSM and inserts them into a new collection in mongoDB. This new collection is named "CVCounts". This new collection is useful for the CV Manager to query the message counts in a performant manner. This script runs on a cron every 24 hours.

It is not recommended to change the frequency of this counter to allow the CV Manager's API to properly query for counts. If the daily emailer has been configured properly, this script will automatically run and maintain itself.
