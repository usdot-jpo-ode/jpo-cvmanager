package us.dot.its.jpo.ode.api;

import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


import com.google.common.collect.ImmutableList;
import org.apache.kafka.streams.KafkaStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;

import org.springframework.stereotype.Controller;

import us.dot.its.jpo.conflictmonitor.monitor.algorithms.StreamsTopology;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.ConnectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.LaneDirectionOfTravelAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.assessments.StopLinePassageAssessment;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.IntersectionReferenceAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.TimeChangeDetailsEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.ConnectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.IntersectionReferenceAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.LaneDirectionOfTravelNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.conflictmonitor.monitor.serialization.JsonSerdes;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.api.topologies.*;
import us.dot.its.jpo.ode.model.OdeSpatData;
import us.dot.its.jpo.ode.model.OdeBsmData;
import us.dot.its.jpo.ode.model.OdeMapData;

import us.dot.its.jpo.ode.api.accessors.assessments.ConnectionOfTravelAssessment.ConnectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.LaneDirectionOfTravelAssessment.LaneDirectionOfTravelAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateAssessment.StopLineStopAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.assessments.SignalStateEventAssessment.SignalStateEventAssessmentRepository;
import us.dot.its.jpo.ode.api.accessors.bsm.OdeBsmJsonRepository;
import us.dot.its.jpo.ode.api.accessors.config.DefaultConfig.DefaultConfigRepository;
import us.dot.its.jpo.ode.api.accessors.config.IntersectionConfig.IntersectionConfigRepository;
import us.dot.its.jpo.ode.api.accessors.events.BsmEvent.BsmEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.ConnectionOfTravelEvent.ConnectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.IntersectionReferenceAlignmentEvent.IntersectionReferenceAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.LaneDirectionOfTravelEvent.LaneDirectionOfTravelEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalGroupAlignmentEvent.SignalGroupAlignmentEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateConflictEvent.SignalStateConflictEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateEvent.SignalStateEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.SignalStateStopEvent.SignalStateStopEventRepository;
import us.dot.its.jpo.ode.api.accessors.events.TimeChangeDetailsEvent.TimeChangeDetailsEventRepository;
import us.dot.its.jpo.ode.api.accessors.map.OdeMapDataRepository;
import us.dot.its.jpo.ode.api.accessors.map.ProcessedMapRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.ActiveNotification.ActiveNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.ConnectionOfTravelNotification.ConnectionOfTravelNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.IntersectionReferenceAlignmentNotification.IntersectionReferenceAlignmentNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.LaneDirectionOfTravelNotificationRepo.LaneDirectionOfTravelNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.MapBroadcastRateNotification.MapBroadcastRateNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SignalGroupAlignmentNotificationRepo.SignalGroupAlignmentNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SignalStateConflictNotification.SignalStateConflictNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.notifications.SpatBroadcastRateNotification.SpatBroadcastRateNotificationRepository;
import us.dot.its.jpo.ode.api.accessors.spat.OdeSpatDataRepository;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.api.controllers.StompController;
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

    // Collection of all the topologies
    @Getter
    final List<RestartableTopology> topologies;

    @Autowired
    public APIServiceController(
            KafkaAdmin admin,
            ConflictMonitorApiProperties props,
            ProcessedSpatRepository processedSpatRepo,
            ProcessedMapRepository processedMapRepo,
            OdeBsmJsonRepository odeBsmJsonRepo,
            OdeSpatDataRepository odeSpatDataRepo,
            OdeMapDataRepository odeMapDataRepo,
            LaneDirectionOfTravelAssessmentRepository laneDirectionOfTravelAssessmentRepo,
            ConnectionOfTravelAssessmentRepository connectionOfTravelAssessmentRepo,
            StopLineStopAssessmentRepository signalStateAssessmentRepo,
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
            ConnectionOfTravelNotificationRepository connectionOfTravelNotificationRepo,
            BsmEventRepository bsmEventRepo,
            ActiveNotificationRepository activeNotificationRepo,
            StompController stompController) {

        ImmutableList.Builder<RestartableTopology> topologyListBuilder = ImmutableList.builder();

        try {

            logger.info("Starting {}", this.getClass().getSimpleName());


            SpatSocketForwardTopology spatSocketForwardTopology = new SpatSocketForwardTopology(
                "topic.ProcessedSpat",
                stompController,
                props.createStreamProperties("processedSpat")
            );
            topologyListBuilder.add(spatSocketForwardTopology);

            MapSocketForwardTopology mapSocketForwardTopology = new MapSocketForwardTopology(
                "topic.ProcessedMap",
                stompController,
                props.createStreamProperties("processedMap")
            );
            topologyListBuilder.add(mapSocketForwardTopology);

            BsmSocketForwardTopology bsmSocketForwardTopology = new BsmSocketForwardTopology(
                "topic.CmBsmIntersection",
                stompController,
                props.createStreamProperties("bsm")
            );
            topologyListBuilder.add(bsmSocketForwardTopology);

            logger.info("load = {}", props.getLoad());
            if (props.getLoad()) {
                
                ArrayList<String> topics = new ArrayList<>();
                DataLoaderTopology<OdeBsmData> odeBsmJsonTopology = new DataLoaderTopology<OdeBsmData>(
                        "topic.OdeBsmJson",
                        JsonSerdes.OdeBsm(),
                        odeBsmJsonRepo,
                        props.createStreamProperties("odeBsmJson"));
                topics.add("topic.OdeBsmJson");
                topologyListBuilder.add(odeBsmJsonTopology);

                DataLoaderTopology<OdeMapData> odeMapJsonTopology = new DataLoaderTopology<OdeMapData>(
                        "topic.OdeMapJson",
                        us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.OdeMap(),
                        odeMapDataRepo,
                        props.createStreamProperties("odeMapData"));
                topics.add("topic.OdeMapJson");
                topologyListBuilder.add(odeMapJsonTopology);

                DataLoaderTopology<OdeSpatData> odeSpatJsonTopology = new DataLoaderTopology<OdeSpatData>(
                        "topic.OdeSpatJson",
                        us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.OdeSpat(),
                        odeSpatDataRepo,
                        props.createStreamProperties("odeSpatData"));
                topics.add("topic.OdeSpatJson");
                topologyListBuilder.add(odeSpatJsonTopology);

                
                DataLoaderTopology<ProcessedSpat> processedSpatTopology = new DataLoaderTopology<ProcessedSpat>(
                        "topic.ProcessedSpat",
                        us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.ProcessedSpat(),
                        processedSpatRepo,
                        props.createStreamProperties("processedSpat"));
                topics.add("topic.ProcessedSpat");
                topologyListBuilder.add(processedSpatTopology);

                DataLoaderTopology<ProcessedMap<LineString>> processedMapTopology = new DataLoaderTopology<ProcessedMap<LineString>>(
                        "topic.ProcessedMap",
                        us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.ProcessedMapGeoJson(),
                        processedMapRepo,
                        props.createStreamProperties("processedMap"));
                topics.add("topic.ProcessedMap");
                topologyListBuilder.add(processedMapTopology);

                DataLoaderTopology<ConnectionOfTravelEvent> connectionOfTravelEventTopology = new DataLoaderTopology<ConnectionOfTravelEvent>(
                        "topic.CmConnectionOfTravelEvent",
                        JsonSerdes.ConnectionOfTravelEvent(),
                        connectionOfTravelEventRepo,
                        props.createStreamProperties("connectionOfTravelEvent"));
                topics.add("topic.CmConnectionOfTravelEvent");
                topologyListBuilder.add(connectionOfTravelEventTopology);

                DataLoaderTopology<IntersectionReferenceAlignmentEvent> intersectionReferenceAlignmentEventTopology = new DataLoaderTopology<IntersectionReferenceAlignmentEvent>(
                        "topic.CmIntersectionReferenceAlignmentEvents",
                        JsonSerdes.IntersectionReferenceAlignmentEvent(),
                        intersectionReferenceAlignmentEventRepo,
                        props.createStreamProperties("intersectionReferenceAlignmentEvent"));
                topics.add("topic.CmIntersectionReferenceAlignmentEvents");
                topologyListBuilder.add(intersectionReferenceAlignmentEventTopology);

                DataLoaderTopology<LaneDirectionOfTravelEvent> laneDirectionOfTravelEventTopology = new DataLoaderTopology<LaneDirectionOfTravelEvent>(
                        "topic.CmLaneDirectionOfTravelEvent",
                        JsonSerdes.LaneDirectionOfTravelEvent(),
                        laneDirectionOfTravelEventRepo,
                        props.createStreamProperties("connectionOfTravelEvent"));
                topics.add("topic.CmLaneDirectionOfTravelEvent");
                topologyListBuilder.add(laneDirectionOfTravelEventTopology);

                DataLoaderTopology<SignalGroupAlignmentEvent> signalGroupAlignmentEventTopology = new DataLoaderTopology<SignalGroupAlignmentEvent>(
                        "topic.CmSignalGroupAlignmentEvents",
                        JsonSerdes.SignalGroupAlignmentEvent(),
                        signalGroupAlignmentEventRepo,
                        props.createStreamProperties("signalGroupAlignmentEvent"));
                topics.add("topic.CmSignalGroupAlignmentEvents");
                topologyListBuilder.add(signalGroupAlignmentEventTopology);

                DataLoaderTopology<SignalStateConflictEvent> signalStateConflictEventTopology = new DataLoaderTopology<SignalStateConflictEvent>(
                        "topic.CmSignalStateConflictEvents",
                        JsonSerdes.SignalStateConflictEvent(),
                        signalStateConflictEventRepo,
                        props.createStreamProperties("signalStateConflictEvent"));
                topics.add("topic.CmSignalStateConflictEvents");
                topologyListBuilder.add(signalStateConflictEventTopology);

                // DataLoaderTopology<StopLinePassageEvent> signalStateEventTopology = new DataLoaderTopology<StopLinePassageEvent>(
                //         "topic.CmSignalStateEvent",
                //         JsonSerdes.StopLinePassageEvent(),
                //         signalStateEventRepo,
                //         props.createStreamProperties("signalStateEvent"));
                //         topics.add("topic.CmSignalStateEvent");

                // DataLoaderTopology<StopLineStopEvent> signalStateStopEventTopology = new DataLoaderTopology<SignalStateStopEvent>(
                //         "topic.CmSignalStopEvent",
                //         JsonSerdes.StopLineStopEvent(),
                //         signalStateStopEventRepo,
                //         props.createStreamProperties("signalStateStopEvent"));
                //         topics.add("topic.CmSignalStopEvent");

                DataLoaderTopology<TimeChangeDetailsEvent> timeChangeDetailsEventTopology = new DataLoaderTopology<TimeChangeDetailsEvent>(
                        "topic.CmSpatTimeChangeDetailsEvent",
                        JsonSerdes.TimeChangeDetailsEvent(),
                        timeChangeDetailsEventRepo,
                        props.createStreamProperties("timeChangeDetailsEvent"));
                topics.add("topic.CmSpatTimeChangeDetailsEvent");
                topologyListBuilder.add(timeChangeDetailsEventTopology);

                DataLoaderTopology<ConnectionOfTravelAssessment> connectionOfTravelAssessmentTopology = new DataLoaderTopology<ConnectionOfTravelAssessment>(
                        "topic.CmConnectionOfTravelAssessment",
                        JsonSerdes.ConnectionOfTravelAssessment(),
                        connectionOfTravelAssessmentRepo,
                        props.createStreamProperties("connectionOfTravelAssessment"));
                topics.add("topic.CmConnectionOfTravelAssessment");
                topologyListBuilder.add(connectionOfTravelAssessmentTopology);

                DataLoaderTopology<LaneDirectionOfTravelAssessment> laneDirectionOfTravelAssessmentTopology = new DataLoaderTopology<LaneDirectionOfTravelAssessment>(
                        "topic.CmLaneDirectionOfTravelAssessment",
                        JsonSerdes.LaneDirectionOfTravelAssessment(),
                        laneDirectionOfTravelAssessmentRepo,
                        props.createStreamProperties("laneDirectionOfTravelAssessment"));
                topics.add("topic.CmLaneDirectionOfTravelAssessment");
                topologyListBuilder.add(laneDirectionOfTravelAssessmentTopology);

                // DataLoaderTopology<SignalStateAssessment> signalStateAssessmentTopology = new
                // DataLoaderTopology<SignalStateAssessment>(
                // "topic.CmSignalStateAssessment",
                // JsonSerdes.SignalStateAssessment(),
                // signalStateAssessmentRepo,
                // props.createStreamProperties("signalStateAssessment")
                // );

                DataLoaderTopology<StopLinePassageAssessment> signalStateEventAssessmentTopology = new DataLoaderTopology<StopLinePassageAssessment>(
                        "topic.CmSignalStateEventAssessment",
                        JsonSerdes.StopLinePassageAssessment(),
                        signalStateEventAssessmentRepo,
                        props.createStreamProperties("signalStateEventAssessment"));
                topics.add("topic.CmSignalStateEventAssessment");
                topologyListBuilder.add(signalStateEventAssessmentTopology);

                DataLoaderTopology<ConnectionOfTravelNotification> connectionOfTravelNotificationTopology = new DataLoaderTopology<ConnectionOfTravelNotification>(
                        "topic.CmConnectionOfTravelNotification",
                        JsonSerdes.ConnectionOfTravelNotification(),
                        connectionOfTravelNotificationRepo,
                        props.createStreamProperties("connectionOfTravelNotification"));
                topics.add("topic.CmConnectionOfTravelNotification");
                topologyListBuilder.add(connectionOfTravelNotificationTopology);

                DataLoaderTopology<IntersectionReferenceAlignmentNotification> intersectionReferenceAlignmentNotificationTopology = new DataLoaderTopology<IntersectionReferenceAlignmentNotification>(
                        "topic.CmIntersectionReferenceAlignmentNotification",
                        JsonSerdes.IntersectionReferenceAlignmentNotification(),
                        intersectionReferenceAlignmentNotificationRepo,
                        props.createStreamProperties("intersectionReferenceAlignmentNotification"));
                topics.add("topic.CmIntersectionReferenceAlignmentNotification");
                topologyListBuilder.add(intersectionReferenceAlignmentNotificationTopology);

                DataLoaderTopology<LaneDirectionOfTravelNotification> laneDirectionOfTravelNotificationTopology = new DataLoaderTopology<LaneDirectionOfTravelNotification>(
                        "topic.CmLaneDirectionOfTravelNotification",
                        JsonSerdes.LaneDirectionOfTravelAssessmentNotification(),
                        laneDirectionOfTravelNotificationRepo,
                        props.createStreamProperties("laneDirectionOfTravelNotification"));
                topics.add("topic.CmLaneDirectionOfTravelNotification");
                topologyListBuilder.add(laneDirectionOfTravelNotificationTopology);

                // Waiting on Map Broadcast Rate patch
                // DataLoaderTopology<MapBroadcastRateNotification>
                // mapBroadcastRateNotificationTopology = new
                // DataLoaderTopology<MapBroadcastRateNotification>(
                // "topic.CmMapBroadcastRateNotification",
                // JsonSerdes.MapBroadcastRateNotification(),
                // mapBroadcastRateNotificationRepo,
                // props.createStreamProperties("mapBroadcastRateNotification")
                // );

                DataLoaderTopology<SignalGroupAlignmentNotification> signalGroupAlignmentNotificationTopology = new DataLoaderTopology<SignalGroupAlignmentNotification>(
                        "topic.CmSignalGroupAlignmentNotification",
                        JsonSerdes.SignalGroupAlignmentNotification(),
                        signalGroupAlignmentNotificationRepo,
                        props.createStreamProperties("signalGroupAlignmentNotification"));
                topics.add("topic.CmSignalGroupAlignmentNotification");
                topologyListBuilder.add(signalGroupAlignmentNotificationTopology);

                DataLoaderTopology<SignalStateConflictNotification> signalStateConflictNotificationTopology = new DataLoaderTopology<SignalStateConflictNotification>(
                        "topic.CmSignalStateConflictNotification",
                        JsonSerdes.SignalStateConflictNotification(),
                        signalStateConflictNotificationRepo,
                        props.createStreamProperties("signalStateConflictNotification"));
                topics.add("topic.CmSignalStateConflictNotification");
                topologyListBuilder.add(signalStateConflictNotificationTopology);

                // Waiting on Spat Broadcast Rate Patch
                // DataLoaderTopology<SpatBroadcastRateNotification>
                // spatBroadcastRateNotificationTopology = new
                // DataLoaderTopology<SpatBroadcastRateNotification>(
                // "topic.CmSpatBroadcastRateNotification",
                // JsonSerdes.SpatBroadcastRateNotification(),
                // spatBroadcastRateNotificationRepo,
                // props.createStreamProperties("spatBroadcastRateNotification")
                // );

                DataLoaderTopology<Notification> notificationTopology = new DataLoaderTopology<Notification>(
                        "topic.CmNotification",
                        JsonSerdes.Notification(),
                        activeNotificationRepo,
                        props.createStreamProperties("activeNotification"));
                topics.add("topic.CmNotification");
                topologyListBuilder.add(notificationTopology);

                DataLoaderTopology<BsmEvent> bsmEventsTopology = new DataLoaderTopology<BsmEvent>(
                        "topic.CMBsmEvents",
                        JsonSerdes.BsmEvent(),
                        bsmEventRepo,
                        props.createStreamProperties("bsmEvents"));
                topics.add("topic.CMBsmEvents");
                topologyListBuilder.add(bsmEventsTopology);

                // Missing Topics
                // SpatMinimumDataEvents
                // MapBroadcastRateEvents
                // MapMinimumDataEvents
                // AppHealthNotification
                // OdeRawEncodedBSMJson
                // OdeMapJson
                // OdeRawEncodedMapJson
                // OdeRawEncodedSpatJson
                // OdeSpatJson
                
//                var topicDescMap = admin.describeTopics(topics.toArray(new String[topics.size()]));
//                System.out.println("Found Topics: ");
//                for (var entry : topicDescMap.entrySet()) {
//                    String topicName = entry.getKey();
//                    var desc = entry.getValue();
//                    System.out.println("TopicName: " + topicName +" "+ desc);
//                }
                
                
            }

            logger.info("All Services Constructed {}", this.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Encountered issue with creating topologies", e);
        }

        topologies = topologyListBuilder.build();
    }



}