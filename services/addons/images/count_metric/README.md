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

<b>SMTP_EMAIL_RECIPIENTS:</b> Recipient emails, delimited by ','.

## Kafka JPO-ODE Message Counter

### NOTE: The following is now deprecated. It is recommended to use the emailer feature of this addon service with the mongoDB deployment from the [jpo-conflictmonitor](https://github.com/usdot-jpo-ode/jpo-conflictmonitor/blob/develop/docker-compose.yml) repository with the jpo-ode. The old feature will be removed in the next release (Summer 2024).

This directory contains the script for monitoring a Kafka topic for incoming messages and counting them in regard to the IP/RSU they originated from. (For an example message format, see the last section of the README) Each record is then stored as a custom metric on GCP Monitoring.

To run the script, the following environment variables must be set:

<b>LOGGING_LEVEL:</b> The logging level of the deployment. Options are: 'critical', 'error', 'warning', 'info' and 'debug'. If not specified, will default to 'info'. Refer to Python's documentation for more info: [Python logging](https://docs.python.org/3/howto/logging.html).

<b>MESSAGE_TYPES:</b> Message types to generate counts for separated by commas, ex: 'bsm,tim,map'.

<b>PROJECT_ID:</b> The name of the GCP project the Pub/Sub topics are located in. Must be the same for both the topic and the subscription in this iteration.

<b>ODE_KAFKA_BROKERS:</b> The connection information for the kafka brokers that have the needed kafka streams.

<b>DB_HOST:</b> The connection information for the Postgres database.

<b>PG_DB_USER:</b> Postgres database username.

<b>PG_DB_PASS:</b> Postgres database password, surround in single quotes if this has any special characters.

<b>DB_NAME:</b> Postgres database name.

<b>DESTINATION_DB:</b> Set this to either "MONGODB" or "BIGQUERY" depending on the desired output database.

<b>MONGO_DB_URI:</b> Connection string uri for the MongoDB database, please refer to the following [documentation](https://www.mongodb.com/docs/manual/reference/connection-string/).

<b>MONGO_DB_NAME:</b> MongoDB database name.

<b>INPUT_COUNTS_MONGO_COLLECTION_NAME:</b> MongoDB collection for ODD input message counts.

<b>OUTPUT_COUNTS_MONGO_COLLECTION_NAME:</b> MongoDB collection for ODD output message counts.

<b>KAFKA_BIGQUERY_TABLENAME:</b> This is the name of the BigQuery counts table that will be written to for daily kafka counts.

## Expected Message Content

### Kafka Out Message Example (BSM)

`{"metadata":{"originIp":"172.16.28.41","bsmSource":"RV","logFileName":"","recordType":"bsmTx","securityResultCode":"success","receivedMessageDetails":{"locationData":{"latitude":"","longitude":"","elevation":"","speed":"","heading":""},"rxSource":"RV"},"payloadType":"us.dot.its.jpo.ode.model.OdeBsmPayload","serialId":{"streamId":"3e15a15f-378a-4d41-bef9-a8605059cb3f","bundleSize":1,"bundleId":0,"recordId":0,"serialNumber":0},"odeReceivedAt":"2021-07-21T18:03:16.462Z","schemaVersion":6,"maxDurationTime":0,"odePacketID":"","odeTimStartDateTime":"","recordGeneratedAt":"","sanitized":false},"payload":{"dataType":"us.dot.its.jpo.ode.plugin.j2735.J2735Bsm","data":{"coreData":{"msgCnt":122,"id":"23010C4E","secMark":34600,"position":{"latitude":39.8086235,"longitude":-104.7807546,"elevation":1613.5},"accelSet":{"accelLat":0.00,"accelLong":0.00,"accelVert":0.00,"accelYaw":0.00},"accuracy":{"semiMajor":2.00,"semiMinor":2.00,"orientation":44.9951935489},"transmission":"NEUTRAL","speed":0.00,"heading":0.0000,"brakes":{"wheelBrakes":{"leftFront":false,"rightFront":false,"unavailable":true,"leftRear":false,"rightRear":false},"traction":"unavailable","abs":"unavailable","scs":"unavailable","brakeBoost":"unavailable","auxBrakes":"unavailable"},"size":{"width":200,"length":500}},"partII":[{"id":"VehicleSafetyExtensions","value":{"pathHistory":{"crumbData":[{"elevationOffset":3.1,"heading":0.0,"latOffset":0.0000119,"lonOffset":-0.0000085,"timeOffset":0.01}]},"pathPrediction":{"confidence":0.0,"radiusOfCurve":0.0}}},{"id":"SupplementalVehicleExtensions","value":{}}]}}}`
