/*

This script is the first of two scripts responsible for setting up mongoDB on initial boot. 
These scripts should be copied to the /docker-entrypoint-initdb.d/ directory within the docker image;
the docker image will then execute these scripts automatically when the database is first created. 
This script and its partner are prefixed with the letters a and b respectively to ensure they are run 
in the proper order when copied into the mongoDB docker image.

Since the conflict monitor uses a replica set in its mongoDB configuration. Initializing the replica set
and configuring the collections are separated from one another. The purpose of this to force a reconnect
to the database. This in turn allows the new connection to be connected to the primary replica which is 
required for creating indexes on all the collections. This script is only responsible for creating the 
replica set. Almost all other configuration should go in b_create_indexes.js 

Documentation on how the mongoDB docker image runs startup scripts can be found here
https://hub.docker.com/_/mongo/

*/

const CM_MONGO_ROOT_USERNAME = process.env.MONGO_INITDB_ROOT_USERNAME
const CM_MONGO_ROOT_PASSWORD = process.env.MONGO_INITDB_ROOT_PASSWORD

db = db.getSiblingDB('admin')
db.auth(CM_MONGO_ROOT_USERNAME, CM_MONGO_ROOT_PASSWORD)
db = db.getSiblingDB('ConflictMonitor')

console.log('Initializing Replicas')
try {
  db_status = rs.status()
} catch (err) {
  console.log('Initializing New DB')
  try {
    rs.initiate({ _id: 'rs0', members: [{ _id: 0, host: 'localhost:27017' }] }).ok
  } catch (err) {
    console.log('Unable to Initialize DB')
    console.log(err)
  }
}
