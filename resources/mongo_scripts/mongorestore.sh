#!/bin/bash
if [ "${INSERT_SAMPLE_DATA}" != "true" ]; then
    echo "Skipping mongo restore"
    exit 1
fi

echo "Restoring mongo data"

mongorestore /dump --username ${MONGO_INITDB_ROOT_USERNAME} --password ${MONGO_INITDB_ROOT_PASSWORD}