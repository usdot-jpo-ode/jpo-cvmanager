package us.dot.its.jpo.ode.api;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;


import us.dot.its.jpo.conflictmonitor.ConflictMonitorProperties;
import us.dot.its.jpo.conflictmonitor.StateChangeHandler;
import us.dot.its.jpo.conflictmonitor.StreamsExceptionHandler;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.StreamsTopology;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.serialization.JsonSerdes;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
// import us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes;
import us.dot.its.jpo.ode.api.accessors.assessments.ConnectionOfTravelAssessment.ConnectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment.LaneDirectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateAssessment.SignalStateAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateEventAssessment.SignalStateEventAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepository;
import us.dot.its.jpo.ode.api.accessors.config.DefaultConfig.DefaultConfigRepository;
import us.dot.its.jpo.ode.api.accessors.config.IntersectionConfig.IntersectionConfigRepository;
import us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalGroupAlignmentEvent.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateConflictEvent.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent.SignalStateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent.SignalStateStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification.ConnectionOfTravelNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.IntersectionReferenceAlignmentNotification.IntersectionReferenceAlignmentNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.LaneDirectionOfTravelNotificationRepo.LaneDirectionOfTravelNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.MapBroadcastRateNotification.MapBroadcastRateNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SignalGroupAlignmentNotificationRepo.SignalGroupAlignmentNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SignalStateConflictNotification.SignalStateConflictNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SpatBroadcastRateNotification.SpatBroadcastRateNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepositoryImpl;
import us.dot.its.jpo.ode.api.topologies.DataLoaderTopology;
import lombok.Getter;



/**
 * Launches ToGeoJsonFromJsonConverter service
 */
@Controller
@DependsOn("createKafkaTopics")
@Profile("!test")
public class APIServiceController {

    private static final Logger logger = LoggerFactory.getLogger(APIServiceController.class);
    org.apache.kafka.common.serialization.Serdes bas;

    // Temporary for KafkaStreams that don't implement the Algorithm interface
    @Getter
    final ConcurrentHashMap<String, KafkaStreams> streamsMap = new ConcurrentHashMap<String, KafkaStreams>();

    @Getter
    final ConcurrentHashMap<String, StreamsTopology> algoMap = new ConcurrentHashMap<String, StreamsTopology>();

    @Autowired
    public APIServiceController(
        ConflictMonitorApiProperties props,
        ProcessedSpatRepository processedSpatRepo,
        ProcessedMapRepository processedMapRepo,
        OdeBsmJsonRepository odeBsmJsonRepo,
        LaneDirectionOfTravelAssessmentRepository laneDirectionOfTravelAssessmentRepo,
        ConnectionOfTravelAssessmentRepository connectionOfTravelAssessmentRepo,
        SignalStateAssessmentRepository signalStateAssessmentRepo,
        SignalStateEventAssessmentRepository signalStateEventAssessmentRepo,
        DefaultConfigRepository defaultConfigRepository,
        IntersectionConfigRepository intersectionConfigRepository,
        ConnectionOfTravelEventRepository connectionOfTravelEventRepo,
        IntersectionReferenceAlignmentEventRepository intersectionReferenceAlignmentEventRepo,
        LaneDirectionOfTravelEventRepository laneDirectionOfTravelEventRepo,
        SignalGroupAlignmentEventRepository signalGroupAlignmentEventRepo,
        SignalStateConflictEventRepository signalStateConflictEventRepo,
        SignalStateStopEventRepository signalStateStopEventRepo,
        SignalStateEventRepository signalStateEventRepo,
        TimeChangeDetailsEventRepository timeChangeDetailsEventRepo,
        IntersectionReferenceAlignmentNotificationRepository intersectionReferenceAlignmentNotificationRepo,
        LaneDirectionOfTravelNotificationRepository laneDirectionOfTravelNotificationRepo,
        MapBroadcastRateNotificationRepository mapBroadcastRateNotificationRepo,
        SignalGroupAlignmentNotificationRepository signalGroupAlignmentNotificationRepo,
        SignalStateConflictNotificationRepository signalStateConflictNotificationRepo,
        SpatBroadcastRateNotificationRepository spatBroadcastRateNotificationRepo,
        ConnectionOfTravelNotificationRepository connectionOfTravelNotificationRepo
        ) {

        try {


            logger.info("Starting {}", this.getClass().getSimpleName());


            DataLoaderTopology<ProcessedSpat> processedSpatTopology = new DataLoaderTopology<ProcessedSpat>(
                "topic.ProcessedSpat",
                us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.ProcessedSpat(),
                processedSpatRepo,
                props.createStreamProperties("processedSpat")
            );

            DataLoaderTopology<ProcessedMap<LineString>> processedMapTopology = new DataLoaderTopology<ProcessedMap<LineString>>(
                "topic.ProcessedMap",
                us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.ProcessedMapGeoJson(),
                processedMapRepo,
                props.createStreamProperties("processedMap")
            );

            DataLoaderTopology<ConnectionOfTravelEvent> connectionOfTravelEventTopology = new DataLoaderTopology<ConnectionOfTravelEvent>(
                "topic.CmConnectionOfTravelEvent",
                JsonSerdes.ConnectionOfTravelEvent(),
                connectionOfTravelEventRepo,
                props.createStreamProperties("connectionOfTravelEvent")
            );

            DataLoaderTopology<IntersectionReferenceAlignmentEvent> intersectionReferenceAlignmentEventTopology = new DataLoaderTopology<IntersectionReferenceAlignmentEvent>(
                "topic.CmIntersectionReferenceAlignmentEvents",
                JsonSerdes.IntersectionReferenceAlignmentEvent(),
                intersectionReferenceAlignmentEventRepo,
                props.createStreamProperties("intersectionReferenceAlignmentEvent")
            );

            DataLoaderTopology<LaneDirectionOfTravelEvent> laneDirectionOfTravelEventTopology = new DataLoaderTopology<LaneDirectionOfTravelEvent>(
                "topic.CmLaneDirectionOfTravelEvent",
                JsonSerdes.LaneDirectionOfTravelEvent(),
                laneDirectionOfTravelEventRepo,
                props.createStreamProperties("connectionOfTravelEvent")
            );

            DataLoaderTopology<SignalGroupAlignmentEvent> signalGroupAlignmentEventTopology = new DataLoaderTopology<SignalGroupAlignmentEvent>(
                "topic.CmSignalGroupAlignmentEvents",
                JsonSerdes.SignalGroupAlignmentEvent(),
                signalGroupAlignmentEventRepo,
                props.createStreamProperties("signalGroupAlignmentEvent")
            );

            DataLoaderTopology<SignalStateConflictEvent> signalStateConflictEventTopology = new DataLoaderTopology<SignalStateConflictEvent>(
                "topic.CmSignalStateConflictEvents",
                JsonSerdes.SignalStateConflictEvent(),
                signalStateConflictEventRepo,
                props.createStreamProperties("signalStateConflictEvent")
            );

            DataLoaderTopology<SignalStateEvent> signalStateEventTopology = new DataLoaderTopology<SignalStateEvent>(
                "topic.CmSignalStateEvent",
                JsonSerdes.SignalStateEvent(),
                signalStateEventRepo,
                props.createStreamProperties("signalStateEvent")
            );

            DataLoaderTopology<SignalStateStopEvent> signalStateStopEventTopology = new DataLoaderTopology<SignalStateStopEvent>(
                "topic.CmSignalStopEvent",
                JsonSerdes.SignalStateVehicleStopsEvent(),
                signalStateStopEventRepo, 
                props.createStreamProperties("signalStateStopEvent")
            );

            DataLoaderTopology<TimeChangeDetailsEvent> timeChangeDetailsEventTopology = new DataLoaderTopology<TimeChangeDetailsEvent>(
                "topic.CmSpatTimeChangeDetailsEvent",
                JsonSerdes.TimeChangeDetailsEvent(),
                timeChangeDetailsEventRepo, 
                props.createStreamProperties("timeChangeDetailsEvent")
            );


            
            
            // String topicName, Class dataType, Serde consumerSerde, DataLoader loader, Properties streamsProperties

            // final String notification = "notification";
            // final NotificationAlgorithmFactory notificationAlgoFactory = conflictMonitorProps.getNotificationAlgorithmFactory();
            // final String notAlgo = conflictMonitorProps.getNotificationAlgorithm();
            // final NotificationAlgorithm notificationAlgo = notificationAlgoFactory.getAlgorithm(notAlgo);
            // final NotificationParameters notificationParams = conflictMonitorProps.getNotificationAlgorithmParameters();
            // if (notificationAlgo instanceof StreamsTopology) {     
            //     final var streamsAlgo = (StreamsTopology)notificationAlgo;
            //     streamsAlgo.setStreamsProperties(conflictMonitorProps.createStreamProperties(notification));
            //     streamsAlgo.registerStateListener(new StateChangeHandler(kafkaTemplate, notification, stateChangeTopic, healthTopic));
            //     streamsAlgo.registerUncaughtExceptionHandler(new StreamsExceptionHandler(kafkaTemplate, notification, healthTopic));
            //     algoMap.put(notification, streamsAlgo);
            // }
            // notificationAlgo.setParameters(notificationParams);
            // Runtime.getRuntime().addShutdownHook(new Thread(notificationAlgo::stop));
            // notificationAlgo.start();
           

            logger.info("All Services Constructed {}", this.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Encountered issue with creating topologies", e);
        }
    }

    
}