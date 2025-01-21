> **WARNING**: This module is deprecated and will be removed in a future release.

# GeoSpatial Message Query Utility

Service that creates a geospatially queryable MongoDB collection for use with the CV manager.

To run the script, the following environment variables must be set:

<b>LOGGING_LEVEL:</b> The logging level of the deployment. Options are: 'critical', 'error', 'warning', 'info' and 'debug'. If not specified, will default to 'info'. Refer to Python's documentation for more info: [Python logging](https://docs.python.org/3/howto/logging.html).

<b>MONGO_DB_URI:</b> Connection string uri for the MongoDB database, please refer to the following [documentation](https://www.mongodb.com/docs/manual/reference/connection-string/).

<b>MONGO_DB_NAME:</b> MongoDB database name.

<b>MONGO_INPUT_COLLECTIONS:</b> MongoDB collection for the input of the service, eg: 'OdeBsmJson,OdePsmJson'

<b>MONGO_GEO_OUTPUT_COLLECTION:</b> MongoDB collection that will be created by the bsm_query script. It will also create an index for better geospatial query performance.

<b>MONGO_TTL:</b> Time to live in days for messages produced by this service. This will create a TTL index in the output Mongo collection.
