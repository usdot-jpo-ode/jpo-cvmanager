// Create indexes on all collections

/*
This is the second script responsible for configuring mongoDB automatically on startup.
This script is responsible for creating users, creating collections, adding indexes, and configuring TTLs
For more information see the header in a_init_replicas.js
*/

console.log("");
console.log("Running create_indexes.js");


// Setup Username and Password Definitions
const CM_MONGO_ROOT_USERNAME = process.env.CM_MONGO_ROOT_USERNAME;
const CM_MONGO_ROOT_PASSWORD = process.env.CM_MONGO_ROOT_PASSWORD;

const CM_MONGO_CONNECTOR_USERNAME = process.env.CM_MONGO_CONNECTOR_USERNAME;
const CM_MONGO_CONNECTOR_PASSWORD = process.env.CM_MONGO_CONNECTOR_PASSWORD;

const CM_MONGO_API_USERNAME = process.env.CM_MONGO_API_USERNAME;
const CM_MONGO_API_PASSWORD = process.env.CM_MONGO_API_PASSWORD;

const CM_MONGO_USER_USERNAME = process.env.CM_MONGO_USER_USERNAME;
const CM_MONGO_USER_PASSWORD = process.env.CM_MONGO_USER_PASSWORD;

const CM_DATABASE_NAME = process.env.CM_DATABASE_NAME || "ConflictMonitor";

const expireSeconds = 5184000; // 2 months
const retryMilliseconds = 10000;


const users = [
    // {username: CM_MONGO_ROOT_USERNAME, password: CM_MONGO_ROOT_PASSWORD, roles: "root", database: "admin" },
    {username: CM_MONGO_CONNECTOR_USERNAME, password: CM_MONGO_CONNECTOR_PASSWORD, roles: "readWrite", database: CM_DATABASE_NAME},
    {username: CM_MONGO_API_USERNAME, password: CM_MONGO_API_PASSWORD, roles: "readWrite", database: CM_DATABASE_NAME},
    {username: CM_MONGO_USER_USERNAME, password: CM_MONGO_USER_PASSWORD, roles: "read", database: CM_DATABASE_NAME},
];


// name -> collection name
// ttlField -> field to perform ttl on 
// timeField -> field to index for time queries
// intersectionField -> field containing intersection id for id queries
const collections = [

    // ODE Json data
    {name: "OdeDriverAlertJson", ttlField: "recordGeneratedAt", "timeField": "metadata.odeReceivedAt", intersectionField: null, rsuIP:"metadata.originIp"},
    {name: "OdeBsmJson", ttlField: "recordGeneratedAt", "timeField": "metadata.odeReceivedAt", intersectionField: null, rsuIP:"metadata.originIp"},
    {name: "OdeMapJson", ttlField: "recordGeneratedAt", "timeField": "metadata.odeReceivedAt", intersectionField: null, rsuIP:"metadata.originIp"},
    {name: "OdeSpatJson", ttlField: "recordGeneratedAt", "timeField": "metadata.odeReceivedAt", intersectionField: null, rsuIP:"metadata.originIp"},
    {name: "OdeSpatRxJson", ttlField: "recordGeneratedAt", "timeField": "metadata.odeReceivedAt", intersectionField: null, rsuIP:"metadata.originIp"},
    {name: "OdeSrmJson", ttlField: "recordGeneratedAt", "timeField": "metadata.odeReceivedAt", intersectionField: null, rsuIP:"metadata.originIp"},
    {name: "OdeSsmJson", ttlField: "recordGeneratedAt", "timeField": "metadata.odeReceivedAt", intersectionField: null, rsuIP:"metadata.originIp"},
    {name: "OdeTimJson", ttlField: "recordGeneratedAt", "timeField": "metadata.odeReceivedAt", intersectionField: null, rsuIP:"metadata.originIp"},
    {name: "OdeTimBroadcastJson", ttlField: "recordGeneratedAt", "timeField": "metadata.odeReceivedAt", intersectionField: null, rsuIP:"metadata.originIp"},
    {name: "OdeTIMCertExpirationTimeJson", ttlField: "recordGeneratedAt", "timeField": "metadata.odeReceivedAt", intersectionField: null, rsuIP:"metadata.originIp"},

    // Ode Raw ASN
    {name: "OdeRawEncodedBSMJson", ttlField: "recordGeneratedAt", "timeField": "BsmMessageContent.metadata.utctimestamp", intersectionField: null, rsuIP:"BsmMessageContent.metadata.originRsu"},
    {name: "OdeRawEncodedMAPJson", ttlField: "recordGeneratedAt", "timeField": "MapMessageContent.metadata.utctimestamp", intersectionField: null, rsuIP:"MapMessageContent.metadata.originRsu"},
    {name: "OdeRawEncodedSPATJson", ttlField: "recordGeneratedAt", "timeField": "SpatMessageContent.metadata.utctimestamp", intersectionField: null, rsuIP:"SpatMessageContent.metadata.originRsu"},
    {name: "OdeRawEncodedSRMJson", ttlField: "recordGeneratedAt", "timeField": "SrmMessageContent.metadata.utctimestamp", intersectionField: null, rsuIP:"SpatMessageContent.metadata.originRsu"},
    {name: "OdeRawEncodedSSMJson", ttlField: "recordGeneratedAt", "timeField": "SsmMessageContent.metadata.utctimestamp", intersectionField: null, rsuIP:"SpatMessageContent.metadata.originRsu"},
    {name: "OdeRawEncodedTIMJson", ttlField: "recordGeneratedAt", "timeField": "TimMessageContent.metadata.utctimestamp", intersectionField: null, rsuIP:"SpatMessageContent.metadata.originRsu"},
    
    // GeoJson Converter Data
    {name: "ProcessedMap", ttlField: "recordGeneratedAt", timeField: "properties.timeStamp", intersectionField: "properties.intersectionId"},
    {name: "ProcessedSpat", ttlField: "recordGeneratedAt", timeField: "utcTimeStamp", intersectionField: "intersectionId"},
    
    // Conflict Monitor Events
    { name: "CmStopLineStopEvent", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmStopLinePassageEvent", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmIntersectionReferenceAlignmentEvents", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmSignalGroupAlignmentEvents", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmConnectionOfTravelEvent", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmSignalStateConflictEvents", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmLaneDirectionOfTravelEvent", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmSpatTimeChangeDetailsEvent", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmSpatMinimumDataEvents", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmMapBroadcastRateEvents", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmMapMinimumDataEvents", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmSpatBroadcastRateEvents", ttlField: "eventGeneratedAt", timeField: "eventGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmBsmEvents", ttlField: "recordGeneratedAt", timeField: "recordGeneratedAt", intersectionField: "intersectionID" },

    // Conflict Monitor Assessments
    { name: "CmLaneDirectionOfTravelAssessment", ttlField: "assessmentGeneratedAt", timeField: "assessmentGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmConnectionOfTravelAssessment", ttlField: "assessmentGeneratedAt", timeField: "assessmentGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmSignalStateEventAssessment", ttlField: "assessmentGeneratedAt", timeField: "assessmentGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmStopLineStopAssessment", ttlField: "assessmentGeneratedAt", timeField: "assessmentGeneratedAt", intersectionField: "intersectionID" },
    
    // Conflict Monitor Notifications
    { name: "CmSpatTimeChangeDetailsNotification", ttlField: "notificationGeneratedAt", timeField: "notificationGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmLaneDirectionOfTravelNotification", ttlField: "notificationGeneratedAt", timeField: "notificationGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmConnectionOfTravelNotification", ttlField: "notificationGeneratedAt", timeField: "notificationGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmAppHealthNotifications", ttlField: "notificationGeneratedAt", timeField: "notificationGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmSignalStateConflictNotification", ttlField: "notificationGeneratedAt", timeField: "notificationGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmSignalGroupAlignmentNotification", ttlField: "notificationGeneratedAt", timeField: "notificationGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmStopLinePassageNotification", ttlField: "notificationGeneratedAt", timeField: "notificationGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmStopLineStopNotification", ttlField: "notificationGeneratedAt", timeField: "notificationGeneratedAt", intersectionField: "intersectionID" },
    { name: "CmNotification", ttlField: "notificationGeneratedAt", timeField: "notificationGeneratedAt", intersectionField: "intersectionID" },

    // Mongo Management Collection
    { name: "MongoStorage", ttlField: null, timeField: "recordGeneratedAt", intersectionField: null }
];

try{
    db.getMongo().setReadPref("primaryPreferred");
    db = db.getSiblingDB("ConflictMonitor");
    db.getMongo().setReadPref("primaryPreferred");
    var isMaster = db.isMaster();
    if (isMaster.primary) {
        console.log("Connected to the primary replica set member.");
    } else {
        console.log("Not connected to the primary replica set member. Current node: " + isMaster.host);
    }
} 
catch(err){
    console.log("Could not switch DB to Sibling DB");
    console.log(err);
}

// Create Users in Database
for(user of users){
    createUser(user);
}

// Wait for the collections to exist in mongo before trying to create indexes on them
let missing_collection_count;
do {
    try {
        missing_collection_count = 0;
        const collectionNames = db.getCollectionNames();
        for (collection of collections) {
            // Create Collection if it doesn't exist
            let created = false;
            if(!collectionNames.includes(collection.name)){
                created = createCollection(collection);
                // created = true;
            }else{
                created = true;
            }

            if(created){
                createTTLIndex(collection);
                createTimeIntersectionIndex(collection);
                createTimeRsuIpIndex(collection);
                createTimeIndex(collection);
            }else{
                missing_collection_count++;
                console.log("Collection " + collection.name + " does not exist yet");
            }
        }
        if (missing_collection_count > 0) {
            console.log("Waiting on " + missing_collection_count + " collections to be created...will try again in " + retryMilliseconds + " ms");
            sleep(retryMilliseconds);
        }
    } catch (err) {
        console.log("Error while setting up TTL indexs in collections");
        console.log(rs.status());
        console.error(err);
        sleep(retryMilliseconds);
    }
} while (missing_collection_count > 0);

console.log("Finished Creating All TTL indexes");

function createUser(user){
    try{
        console.log("Creating User: " + user.username + " with Permissions: " + user.roles);
        db.createUser(
        {
            user: user.username,
            pwd: user.password,
            roles: [
                { role: user.roles, db: user.database },
            ]
        });

    }catch (err){
        console.log(err);
        console.log("Unable to Create User. Perhaps the User already exists.");
    }
}

function createCollection(collection){
    try {
        db.createCollection(collection.name);
        return true;
    } catch (err) {
        console.log("Unable to Create Collection: " + collection.name);
        console.log(err);
        return false;
    }
}

// Create TTL Indexes
function createTTLIndex(collection) {
    try{
        if(collection.hasOwnProperty("ttlField") && collection.ttlField != null){
            const ttlField = collection.ttlField;
            const collectionName = collection.name;
            
            let indexJson = {};
            indexJson[ttlField] = 1;

            if (ttlIndexExists(collection)) {
                db.runCommand({
                    "collMod": collectionName,
                    "index": {
                        keyPattern: indexJson,
                        expireAfterSeconds: expireSeconds
                    }
                });
                console.log("Updated TTL index for " + collectionName + " using the field: " + ttlField + " as the timestamp");
            }else{
                db[collectionName].createIndex(indexJson,
                    {expireAfterSeconds: expireSeconds}
                );
                console.log("Created TTL index for " + collectionName + " using the field: " + ttlField + " as the timestamp");
            }
        }
    } catch(err){
        console.log("Failed to Create or Update index for " + collectionName + "using the field: " + ttlField + " as the timestamp");
    }
}

function createTimeIndex(collection){
    if(timeIndexExists(collection)){
        // Skip if Index already Exists
        return;
    }

    if(collection.hasOwnProperty("timeField") && collection.timeField != null){
        const collectionName = collection.name;
        const timeField = collection.timeField;
        console.log("Creating Time Index for " + collectionName);

        var indexJson = {};
        indexJson[timeField] = -1;

        try {
            db[collectionName].createIndex(indexJson);
            console.log("Created Time Intersection index for " + collectionName + " using the field: " + timeField + " as the timestamp");
        } catch (err) {
            db.runCommand({
                "collMod": collectionName,
                "index": {
                    keyPattern: indexJson
                }
            });
            console.log("Updated Time index for " + collectionName + " using the field: " + timeField + " as the timestamp");
        }
    }
}

function createTimeRsuIpIndex(){
    if(timeRsuIpIndexExists(collection)){
        // Skip if Index already Exists
        return;
    }

    if(collection.hasOwnProperty("timeField") && collection.timeField != null && collection.hasOwnProperty("rsuIP") && collection.rsuIP != null){
        const collectionName = collection.name;
        const timeField = collection.timeField;
        const rsuIP = collection.rsuIP;
        console.log("Creating Time rsuIP Index for " + collectionName);

        var indexJson = {};
        indexJson[rsuIP] = -1;
        indexJson[timeField] = -1;
        

        try {
            db[collectionName].createIndex(indexJson);
            console.log("Created Time rsuIP Intersection index for " + collectionName + " using the field: " + timeField + " as the timestamp and : " + rsuIP+" as the rsuIP");
        } catch (err) {
            db.runCommand({
                "collMod": collectionName,
                "index": {
                    keyPattern: indexJson
                }
            });
            console.log("Updated Time rsuIP index for " + collectionName + " using the field: " + timeField + " as the timestamp and : " + rsuIP+" as the rsuIP");
        }
    }
}


function createTimeIntersectionIndex(collection){
    if(timeIntersectionIndexExists(collection)){
        // Skip if Index already Exists
        return;
    }

    if(collection.hasOwnProperty("timeField") && collection.timeField != null && collection.hasOwnProperty("intersectionField") && collection.intersectionField != null){
        const collectionName = collection.name;
        const timeField = collection.timeField;
        const intersectionField = collection.intersectionField;
        console.log("Creating time intersection index for " + collectionName);

        var indexJson = {};
        indexJson[intersectionField] = -1;
        indexJson[timeField] = -1;
        

        try {
            db[collectionName].createIndex(indexJson);
            console.log("Created time intersection index for " + collectionName + " using the field: " + timeField + " as the timestamp and : " + intersectionField + " as the rsuIP");
        } catch (err) {
            db.runCommand({
                "collMod": collectionName,
                "index": {
                    keyPattern: indexJson
                }
            });
            console.log("Updated time intersection index for " + collectionName + " using the field: " + timeField + " as the timestamp and : " + intersectionField + " as the rsuIP");
        }
    }
}

function ttlIndexExists(collection) {
    return db[collection.name].getIndexes().find((idx) => idx.hasOwnProperty("expireAfterSeconds")) !== undefined;
}

function timeIntersectionIndexExists(collection){
    return db[collection.name].getIndexes().find((idx) => idx.name == collection.intersectionField + "_-1_" + collection.timeField + "_-1") !== undefined;
}

function timeRsuIpIndexExists(collection){
    return db[collection.name].getIndexes().find((idx) => idx.name == collection.rsuIP + "_-1_" + collection.timeField + "_-1") !== undefined;
}

function timeIndexExists(collection){
    return db[collection.name].getIndexes().find((idx) => idx.name == collection.timeField + "_-1") !== undefined;
}
