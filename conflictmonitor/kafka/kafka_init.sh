sleep 2s
/opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --list
echo 'Creating kafka topics'

# Create topics
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeSpatTxPojo" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeSpatPojo" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.FilteredOdeSpatJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeSpatRxPojo" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeBsmJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.FilteredOdeBsmJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.J2735TimBroadcastJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.Asn1DecoderInput" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.Asn1EncoderInput" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.SDWDepositorInput" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeMapTxPojo" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeSsmPojo" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeSrmTxPojo" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdePsmTxPojo" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdePsmJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.Asn1DecoderOutput" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.Asn1EncoderOutput" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmAppHealthNotification" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmAppHealthNotifications" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmAssessment" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmBsmEvents" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmBsmIntersection" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmBsmJsonRepartition" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmConnectionOfTravelAssessment" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmConnectionOfTravelEvent" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmConnectionOfTravelNotification" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmCustomConfigTable" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmDefaultConfigTable" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmEvent" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmIntersectionConfigTable" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmIntersectionReferenceAlignmentEvents" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmIntersectionReferenceAlignmentNotification" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmKafkaStateChangeEvents" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmLaneDirectionOfTravelAssessment" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmLaneDirectionOfTravelEvent" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmLaneDirectionOfTravelNotification" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmMapBoundingBox" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmMapBroadcastRateEvents" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmMapMinimumDataEvents" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmMergedConfigTable" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmNotification" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmSignalGroupAlignmentEvents" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmSignalGroupAlignmentNotification" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmSignalStateConflictEvents" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmSignalStateConflictNotification" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmSignalStateEventAssessment" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmSpatBroadcastRateEvents" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmSpatMinimumDataEvents" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmSpatTimeChangeDetailsEvent" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmSpatTimeChangeDetailsNotification" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmStopLinePassageEvent" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmStopLinePassageNotification" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmStopLineStopAssessment" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmStopLineStopEvent" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.CmStopLineStopNotification" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.DeduplicatedOdeMapJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.DeduplicatedOdeTimJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.DeduplicatedProcessedMap" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.DeduplicatedProcessedMapWKT" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.FilteredOdeBsmJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.FilteredOdeTimJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeBsmJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeBsmPojo" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeDriverAlertJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeMapJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeRawEncodedBSMJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeRawEncodedMAPJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeRawEncodedPSMJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeRawEncodedSPATJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeRawEncodedSRMJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeRawEncodedSSMJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeRawEncodedTIMJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeSpatJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeSpatRxJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeSrmJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeSsmJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeTIMCertExpirationTimeJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeTimBroadcastJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.OdeTimJson" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.ProcessedMap" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.ProcessedMapWKT" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1
/opt/bitnami/kafka/bin/kafka-topics.sh --create --if-not-exists  --topic "topic.ProcessedSpat" --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1



echo 'Kafka created with the following topics:'
/opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --list
exit