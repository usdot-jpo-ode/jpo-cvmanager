//package us.dot.its.jpo.ode.api.topologies;
//
//import lombok.Getter;
//import org.apache.kafka.streams.KafkaStreams;
//import org.apache.kafka.streams.Topology;
//import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
//import org.slf4j.Logger;
//
//import java.time.Duration;
//import java.util.Properties;
//
///**
// * Default implementation of common functionality for topologies
// */
//public abstract class BaseTopology implements RestartableTopology{
//
//    protected abstract Logger getLogger();
//    protected abstract Topology buildTopology();
//
//    protected Topology topology;
//    protected KafkaStreams streams;
//
//    @Getter
//    protected final String topicName;
//
//    protected final Properties streamsProperties;
//
//    public BaseTopology(String topicName, Properties streamsProperties) {
//        this.topicName = topicName;
//        this.streamsProperties = streamsProperties;
//    }
//
//    @Override
//    public void start() {
//        if (streams != null && streams.state().isRunningOrRebalancing()) {
//            throw new IllegalStateException("Start called while streams is already running.");
//        }
//        topology = buildTopology();
//        streams = new KafkaStreams(topology, streamsProperties);
//        if (exceptionHandler != null) streams.setUncaughtExceptionHandler(exceptionHandler);
//        if (stateListener != null) streams.setStateListener(stateListener);
//        streams.start();
//    }
//
//    @Override
//    public void stop() {
//        getLogger().info("Stopping topology for {}", topicName);
//        if (streams != null) {
//            // Trigger streams to shut down without blocking
//            streams.close(Duration.ZERO);
//        }
//    }
//
//    KafkaStreams.StateListener stateListener;
//    public void registerStateListener(KafkaStreams.StateListener stateListener) {
//        this.stateListener = stateListener;
//    }
//
//    StreamsUncaughtExceptionHandler exceptionHandler;
//    public void registerUncaughtExceptionHandler(StreamsUncaughtExceptionHandler exceptionHandler) {
//        this.exceptionHandler = exceptionHandler;
//    }
//
//}
