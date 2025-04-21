# bin/bash
DB_HOST_IP = $1

echo "------------------------------------------"
echo "Kafka connector creation started."
echo "Provided Docker Host IP: $DOCKER_HOST_IP"
echo "------------------------------------------"






# Record BSM JSON Data
declare -A OdeRawEncodedBSMJson=([name]="topic.OdeRawEncodedBSMJson" [collection]="OdeRawEncodedBSMJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A OdeBsmJson=([name]="topic.OdeBsmJson" [collection]="OdeBsmJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)

# Record Map Data
declare -A OdeMapJson=([name]="topic.DeduplicatedOdeMapJson" [collection]="OdeMapJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A ProcessedMap=([name]="topic.DeduplicatedProcessedMap" [collection]="ProcessedMap"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A OdeRawEncodedMAPJson=([name]="topic.OdeRawEncodedMAPJson" [collection]="OdeRawEncodedMAPJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)

# Record Spat Data
declare -A OdeRawEncodedSPATJson=([name]="topic.OdeRawEncodedSPATJson" [collection]="OdeRawEncodedSPATJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A OdeSpatJson=([name]="topic.OdeSpatJson" [collection]="OdeSpatJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A ProcessedSpat=([name]="topic.ProcessedSpat" [collection]="ProcessedSpat"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A OdeSpatRxJson=([name]="topic.OdeSpatRxJson" [collection]="OdeSpatRxJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)

# Record Driver Alert Data
declare -A OdeDriverAlertJson=([name]="topic.OdeDriverAlertJson" [collection]="OdeDriverAlertJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)

# Record SRM Data
declare -A OdeSrmJson=([name]="topic.OdeSrmJson" [collection]="OdeSrmJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A OdeRawEncodedSRMJson=([name]="topic.OdeRawEncodedSRMJson" [collection]="OdeRawEncodedSRMJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)

# Record SSM Data
declare -A OdeSsmJson=([name]="topic.OdeSsmJson" [collection]="OdeSsmJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A OdeRawEncodedSSMJson=([name]="topic.OdeRawEncodedSSMJson" [collection]="OdeRawEncodedSSMJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)


# Record TIM JSON Data
declare -A OdeTimJson=([name]="topic.DeduplicatedOdeTimJson" [collection]="OdeTimJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A OdeTimBroadcastJson=([name]="topic.OdeTimBroadcastJson" [collection]="OdeTimBroadcastJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A OdeTIMCertExpirationTimeJson=([name]="topic.OdeTIMCertExpirationTimeJson" [collection]="OdeTIMCertExpirationTimeJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)
declare -A OdeRawEncodedTIMJson=([name]="topic.OdeRawEncodedTIMJson" [collection]="OdeRawEncodedTIMJson"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)

############################################################################################### 

# Record Events
declare -A CmStopLinePassageEvent=([name]="topic.CmStopLinePassageEvent" [collection]="CmStopLinePassageEvent"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmStopLineStopEvent=([name]="topic.CmStopLineStopEvent" [collection]="CmStopLineStopEvent"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmSignalStateConflictEvents=([name]="topic.CmSignalStateConflictEvents" [collection]="CmSignalStateConflictEvents"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmIntersectionReferenceAlignmentEvents=([name]="topic.CmIntersectionReferenceAlignmentEvents" [collection]="CmIntersectionReferenceAlignmentEvents"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmSignalGroupAlignmentEvents=([name]="topic.CmSignalGroupAlignmentEvents" [collection]="CmSignalGroupAlignmentEvents"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmConnectionOfTravelEvent=([name]="topic.CmConnectionOfTravelEvent" [collection]="CmConnectionOfTravelEvent"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmLaneDirectionOfTravelEvent=([name]="topic.CmLaneDirectionOfTravelEvent" [collection]="CmLaneDirectionOfTravelEvent"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmSpatTimeChangeDetailsEvent=([name]="topic.CmSpatTimeChangeDetailsEvent" [collection]="CmSpatTimeChangeDetailsEvent"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmSpatMinimumDataEvents=([name]="topic.CmSpatMinimumDataEvents" [collection]="CmSpatMinimumDataEvents"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmMapBroadcastRateEvents=([name]="topic.CmMapBroadcastRateEvents" [collection]="CmMapBroadcastRateEvents"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmMapMinimumDataEvents=([name]="topic.CmMapMinimumDataEvents" [collection]="CmMapMinimumDataEvents"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmSpatBroadcastRateEvents=([name]="topic.CmSpatBroadcastRateEvents" [collection]="CmSpatBroadcastRateEvents"
    [convert_timestamp]=true [timefield]="eventGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)

# Record BSM Events
declare -A CmBsmEvents=([name]="topic.CmBsmEvents" [collection]="CmBsmEvents"
    [convert_timestamp]=false [timefield]="" [use_key]=false [key]="" [add_timestamp]=true)


# Record Assessments
declare -A CmLaneDirectionOfTravelAssessment=([name]="topic.CmLaneDirectionOfTravelAssessment" [collection]="CmLaneDirectionOfTravelAssessment"
    [convert_timestamp]=true [timefield]="assessmentGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmConnectionOfTravelAssessment=([name]="topic.CmConnectionOfTravelAssessment" [collection]="CmConnectionOfTravelAssessment"
    [convert_timestamp]=true [timefield]="assessmentGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmSignalStateEventAssessment=([name]="topic.CmSignalStateEventAssessment" [collection]="CmSignalStateEventAssessment"
    [convert_timestamp]=true [timefield]="assessmentGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmStopLineStopAssessment=([name]="topic.CmStopLineStopAssessment" [collection]="CmStopLineStopAssessment"
    [convert_timestamp]=true [timefield]="assessmentGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)


# Record Notifications
declare -A CmSpatTimeChangeDetailsNotification=([name]="topic.CmSpatTimeChangeDetailsNotification" [collection]="CmSpatTimeChangeDetailsNotification"
    [convert_timestamp]=true [timefield]="notificationGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmLaneDirectionOfTravelNotification=([name]="topic.CmLaneDirectionOfTravelNotification" [collection]="CmLaneDirectionOfTravelNotification"
    [convert_timestamp]=true [timefield]="notificationGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmConnectionOfTravelNotification=([name]="topic.CmConnectionOfTravelNotification" [collection]="CmConnectionOfTravelNotification"
    [convert_timestamp]=true [timefield]="notificationGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmAppHealthNotifications=([name]="topic.CmAppHealthNotifications" [collection]="CmAppHealthNotifications"
    [convert_timestamp]=true [timefield]="notificationGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmSignalStateConflictNotification=([name]="topic.CmSignalStateConflictNotification" [collection]="CmSignalStateConflictNotification"
    [convert_timestamp]=true [timefield]="notificationGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmSignalGroupAlignmentNotification=([name]="topic.CmSignalGroupAlignmentNotification" [collection]="CmSignalGroupAlignmentNotification"
    [convert_timestamp]=true [timefield]="notificationGeneratedAt" [use_key]=false [key]="" [add_timestamp]=false)
declare -A CmNotification=([name]="topic.CmNotification" [collection]="CmNotification"
    [convert_timestamp]=true [timefield]="notificationGeneratedAt" [use_key]=true [key]="key" [add_timestamp]=false)
declare -A CmStopLineStopNotification=([name]="topic.CmStopLineStopNotification" [collection]="CmStopLineStopNotification"
    [convert_timestamp]=true [timefield]="notificationGeneratedAt" [use_key]=true [key]="key" [add_timestamp]=false)
declare -A CmStopLinePassageNotification=([name]="topic.CmStopLinePassageNotification" [collection]="CmStopLinePassageNotification"
    [convert_timestamp]=true [timefield]="notificationGeneratedAt" [use_key]=true [key]="key" [add_timestamp]=false)

function createSink() {
    local -n topic=$1
    echo "Creating sink connector for:"
    for val in "${topic[@]}"; do echo $val; done

    local name=${topic[name]}
    local collection=${topic[collection]}
    local timefield=${topic[timefield]}
    local convert_timestamp=${topic[convert_timestamp]}
    local use_key=${topic[use_key]}
    local key=${topic[key]}
    local add_timestamp=${topic[add_timestamp]}

    echo "name=$name"
    echo "collection=$collection"
    echo "timefield=$timefield"
    echo "convert_timestamp=$convert_timestamp"
    echo "add_timestamp=$add_timestamp"

    local connectConfig=' {
        "group.id":"connector-consumer",
        "connector.class":"com.mongodb.kafka.connect.MongoSinkConnector",
        "tasks.max":3,
        "topics":"'$name'",
        "connection.uri":"mongodb://'$CM_MONGO_CONNECTOR_USERNAME':'$CM_MONGO_CONNECTOR_PASSWORD'@'$DB_HOST_IP':27017/database?authSource=ConflictMonitor",
        "database":"ConflictMonitor",
        "collection":"'$collection'",
        "key.converter":"org.apache.kafka.connect.storage.StringConverter",
        "key.converter.schemas.enable":false,
        "value.converter":"org.apache.kafka.connect.json.JsonConverter",
        "value.converter.schemas.enable":false,
        "errors.tolerance": "all",
        "mongo.errors.tolerance": "all",
        "errors.deadletterqueue.topic.name": "",
	    "errors.log.enable": false,
        "errors.log.include.messages": false,
	    "errors.deadletterqueue.topic.replication.factor": 0' 

	    #"errors.deadletterqueue.context.headers.enable": true,
        #"errors.log.enable": false,
        #"errors.log.include.messages": false,
        #"errors.deadletterqueue.topic.replication.factor": 1'    
	    #"errors.deadletterqueue.topic.name": "dlq.'$collection'.sink",


    if [ "$convert_timestamp" == true ]
    then
        local connectConfig=''$connectConfig',
        "transforms": "TimestampConverter",
        "transforms.TimestampConverter.field": "'$timefield'",
        "transforms.TimestampConverter.type": "org.apache.kafka.connect.transforms.TimestampConverter$Value",
        "transforms.TimestampConverter.target.type": "Timestamp"'
    fi

    if [ "$add_timestamp" == true ]
    then
        local connectConfig=''$connectConfig',
        "transforms": "AddTimestamp,AddedTimestampConverter",
        "transforms.AddTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
        "transforms.AddTimestamp.timestamp.field": "recordGeneratedAt",
        "transforms.AddedTimestampConverter.field": "recordGeneratedAt",
        "transforms.AddedTimestampConverter.type": "org.apache.kafka.connect.transforms.TimestampConverter$Value",
        "transforms.AddedTimestampConverter.target.type": "Timestamp"'
    fi

    if [ "$use_key" == true ]
    then
        local connectConfig=''$connectConfig',
        "document.id.strategy": "com.mongodb.kafka.connect.sink.processor.id.strategy.PartialValueStrategy",
        "document.id.strategy.partial.value.projection.list": "'$key'",
        "document.id.strategy.partial.value.projection.type": "AllowList",
        "document.id.strategy.overwrite.existing": true'
    fi

    local connectConfig=''$connectConfig' }'

    echo " Creating connector with Config : $connectConfig"

    curl -X PUT http://localhost:8083/connectors/MongoSink.${name}/config -H "Content-Type: application/json" -d "$connectConfig"
}

createSink OdeRawEncodedBSMJson
createSink OdeBsmJson

createSink OdeMapJson
createSink ProcessedMap
createSink OdeRawEncodedMAPJson

createSink OdeRawEncodedSPATJson
createSink OdeSpatJson
createSink ProcessedSpat
createSink OdeSpatRxJson

createSink OdeDriverAlertJson

createSink OdeSrmJson
createSink OdeRawEncodedSRMJson

createSink OdeSsmJson
createSink OdeRawEncodedSSMJson

createSink OdeTimJson
createSink OdeTimBroadcastJson
createSink OdeTIMCertExpirationTimeJson
createSink OdeRawEncodedTIMJson

createSink CmStopLinePassageEvent
createSink CmStopLineStopEvent
createSink CmSignalStateConflictEvents
createSink CmIntersectionReferenceAlignmentEvents
createSink CmSignalGroupAlignmentEvents
createSink CmConnectionOfTravelEvent
createSink CmLaneDirectionOfTravelEvent
createSink CmSignalStateEvent
createSink CmSpatTimeChangeDetailsEvent
createSink CmSpatMinimumDataEvents
createSink CmMapBroadcastRateEvents
createSink CmMapMinimumDataEvents
createSink CmSpatBroadcastRateEvents

createSink CmBsmEvents

createSink CmLaneDirectionOfTravelAssessment
createSink CmConnectionOfTravelAssessment
createSink CmSignalStateEventAssessment
createSink CmStopLineStopAssessment

createSink CmSpatTimeChangeDetailsNotification
createSink CmLaneDirectionOfTravelNotification
createSink CmConnectionOfTravelNotification
createSink CmAppHealthNotifications
createSink CmSignalStateConflictNotification
createSink CmSignalGroupAlignmentNotification
createSink CmNotification
createSink CmStopLineStopNotification
createSink CmStopLinePassageNotification


echo "----------------------------------"
echo "Kafka connector creation complete!"
echo "----------------------------------"
