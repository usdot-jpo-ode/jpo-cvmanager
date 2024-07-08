#!/bin/bash 

until mongosh --host mongo:27017 --eval 'quit(db.runCommand({ ping: 1 }).ok ? 0 : 2)' &>/dev/null; do
  sleep 1
done

echo "MongoDB is up and running!"

cd /

mongosh -u $MONGO_ADMIN_DB_USER -p $MONGO_ADMIN_DB_PASS --authenticationDatabase admin --host mongo:27017 /create_indexes.js

if [ "$INSERT_SAMPLE_DATA" = "true" ]; then
    echo "Inserting sample data"
    mongosh -u $MONGO_ADMIN_DB_USER -p $MONGO_ADMIN_DB_PASS --authenticationDatabase admin --host mongo:27017 /insert_data.js
fi