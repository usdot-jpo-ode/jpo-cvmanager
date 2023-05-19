# CDOT GCP Count Metric Generator
This directory contains the script for monitoring a Pub/Sub topic for incoming messages and counting them in regard to the IP/RSU they originated from. (For an example message format, see the last section of the README) Each record is then stored as a custom metric on GCP Monitoring.

To run the script, the following environment variables must be set:

<b>LOGGING_LEVEL:</b> The logging level of the deployment. Options are: 'critical', 'error', 'warning', 'info' and 'debug'. If not specified, will default to 'info'. Refer to Python's documentation for more info: [Python logging](https://docs.python.org/3/howto/logging.html).

<b>GOOGLE_APPLICATION_CREDENTIALS:</b> The file path for a GCP service account that has the permission for Pub/Sub Admin, Monitoring Metric Writer access, and BigQuery writing access permissions. This file must be uploaded to GCP via a Secret.

<b>PROJECT_ID:</b> The name of the GCP project the Pub/Sub topics are located in. Must be the same for both the topic and the subscription in this iteration.

<b>SUBSCRIPTION_ID:</b> The name of the Pub/Sub subscription messages will be consumed from for counting.

<b>MESSAGE_TYPE:</b> This is a temporary field that will later be removed, this value is used to prefix the metric names that are created and written to in GCP Monitoring. 

<b>BIGQUERY_TABLENAME:</b> This is the name of the BigQuery counts table that will be written to for daily counts.

<b>RSU_INFO_ENDPOINT:</b> The HTTP endpoint to a GCP Cloud Function or some other endpoint that returns a JSON list of GeoLocation JSON.

This metric counting application must be run in GCP K8S as a single pod.

## Deploying into GCP K8S
The docker image can be deployed in K8S using the helm deployment. Make sure to create the service account using the Terraform deployment before creating the image using helm.

## Creating and Deleting Custom Metrics
Custom metrics do not need to be created first but for testing purposes they can be. The provided Python script [metriccreation.py](metriccreation.py) contains two Python functions that will help with this. Write a Python script utilizing these to create whatever metrics you need. A service account key with Monitoring Metric Writer permissions is required.

1. `pip install --upgrade google-cloud-monitoring`
2. `export GOOGLE_APPLICATION_CREDENTIALS="<dir-path>/<service-account-key.json>"`
3. Good to go!

There is also a provided delete function that requires the full metric's descriptor name to delete the metric. This can be found by the output of the create metric function or in the value of `descriptor.name`.

## Expected Message Content
### Kafka Out Message Example (BSM)
`{"metadata":{"originIp":"172.16.28.41","bsmSource":"RV","logFileName":"","recordType":"bsmTx","securityResultCode":"success","receivedMessageDetails":{"locationData":{"latitude":"","longitude":"","elevation":"","speed":"","heading":""},"rxSource":"RV"},"payloadType":"us.dot.its.jpo.ode.model.OdeBsmPayload","serialId":{"streamId":"3e15a15f-378a-4d41-bef9-a8605059cb3f","bundleSize":1,"bundleId":0,"recordId":0,"serialNumber":0},"odeReceivedAt":"2021-07-21T18:03:16.462Z","schemaVersion":6,"maxDurationTime":0,"odePacketID":"","odeTimStartDateTime":"","recordGeneratedAt":"","sanitized":false},"payload":{"dataType":"us.dot.its.jpo.ode.plugin.j2735.J2735Bsm","data":{"coreData":{"msgCnt":122,"id":"23010C4E","secMark":34600,"position":{"latitude":39.8086235,"longitude":-104.7807546,"elevation":1613.5},"accelSet":{"accelLat":0.00,"accelLong":0.00,"accelVert":0.00,"accelYaw":0.00},"accuracy":{"semiMajor":2.00,"semiMinor":2.00,"orientation":44.9951935489},"transmission":"NEUTRAL","speed":0.00,"heading":0.0000,"brakes":{"wheelBrakes":{"leftFront":false,"rightFront":false,"unavailable":true,"leftRear":false,"rightRear":false},"traction":"unavailable","abs":"unavailable","scs":"unavailable","brakeBoost":"unavailable","auxBrakes":"unavailable"},"size":{"width":200,"length":500}},"partII":[{"id":"VehicleSafetyExtensions","value":{"pathHistory":{"crumbData":[{"elevationOffset":3.1,"heading":0.0,"latOffset":0.0000119,"lonOffset":-0.0000085,"timeOffset":0.01}]},"pathPrediction":{"confidence":0.0,"radiusOfCurve":0.0}}},{"id":"SupplementalVehicleExtensions","value":{}}]}}}`
