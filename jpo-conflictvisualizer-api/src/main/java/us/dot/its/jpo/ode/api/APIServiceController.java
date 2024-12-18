//package us.dot.its.jpo.ode.api;
//
//import java.util.List;
//
//
//import com.google.common.collect.ImmutableList;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.DependsOn;
//import org.springframework.context.annotation.Profile;
//
//import org.springframework.stereotype.Controller;
//
//import us.dot.its.jpo.ode.api.topologies.*;
//
//import us.dot.its.jpo.ode.api.controllers.StompController;
//import lombok.Getter;
//
///**
// * Launches ToGeoJsonFromJsonConverter service
// */
//@Controller
//@DependsOn("createKafkaTopics")
//@Profile("!test")
//public class APIServiceController {
//
//    private static final Logger logger = LoggerFactory.getLogger(APIServiceController.class);
//    org.apache.kafka.common.serialization.Serdes bas;
//
//    // Collection of all the topologies
//    @Getter
//    final List<RestartableTopology> topologies;
//
//    @Autowired
//    public APIServiceController(
//            ConflictMonitorApiProperties props,
//            StompController stompController) {
//
//        ImmutableList.Builder<RestartableTopology> topologyListBuilder = ImmutableList.builder();
//
//        try {
//
//            logger.info("Starting {}", this.getClass().getSimpleName());
//
//
//            SpatSocketForwardTopology spatSocketForwardTopology = new SpatSocketForwardTopology(
//                "topic.ProcessedSpat",
//                stompController,
//                props.createStreamProperties("processedSpat")
//            );
//            topologyListBuilder.add(spatSocketForwardTopology);
//
//            MapSocketForwardTopology mapSocketForwardTopology = new MapSocketForwardTopology(
//                "topic.ProcessedMap",
//                stompController,
//                props.createStreamProperties("processedMap")
//            );
//            topologyListBuilder.add(mapSocketForwardTopology);
//
//            BsmSocketForwardTopology bsmSocketForwardTopology = new BsmSocketForwardTopology(
//                "topic.CmBsmIntersection",
//                stompController,
//                props.createStreamProperties("bsm")
//            );
//            topologyListBuilder.add(bsmSocketForwardTopology);
//
//            logger.info("All Services Constructed {}", this.getClass().getSimpleName());
//        } catch (Exception e) {
//            logger.error("Encountered issue with creating topologies", e);
//        }
//
//        topologies = topologyListBuilder.build();
//    }
//
//
//
//}