package us.dot.its.jpo.ode.api.topologies;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.KafkaStreams.StateListener;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.conflictmonitor.monitor.algorithms.notification.NotificationParameters;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.notification.NotificationStreamsAlgorithm;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.Notification;
import us.dot.its.jpo.conflictmonitor.monitor.serialization.JsonSerdes;
import us.dot.its.jpo.ode.api.accessors.spat.ProcessedSpatRepository;
import us.dot.its.jpo.ode.api.models.DataLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static us.dot.its.jpo.conflictmonitor.monitor.algorithms.notification.NotificationConstants.*;

import java.util.Properties;

public class DataLoaderTopology<T>{

    private static final Logger logger = LoggerFactory.getLogger(DataLoaderTopology.class);

    Topology topology;
    KafkaStreams streams;
    String topicName;
    Serde<T> consumerSerde;
    DataLoader<T> dataLoader;
    Properties streamsProperties;

    public DataLoaderTopology(String topicName, Serde<T> consumerSerde, DataLoader<T> dataLoader, Properties streamsProperties){
        this.topicName = topicName;
        this.consumerSerde = consumerSerde;
        this.dataLoader = dataLoader;
        this.streamsProperties = streamsProperties;
        System.out.println(this.dataLoader);
        this.start();
    }

    
    public void start() {
        if (streams != null && streams.state().isRunningOrRebalancing()) {
            throw new IllegalStateException("Start called while streams is already running.");
        }
        logger.info("Starting Notification Topology.");
        Topology topology = buildTopology();
        streams = new KafkaStreams(topology, streamsProperties);
        if (exceptionHandler != null) streams.setUncaughtExceptionHandler(exceptionHandler);
        if (stateListener != null) streams.setStateListener(stateListener);
        streams.start();
        logger.info("Started Notification Topology");
    }

    public Topology buildTopology() {
        System.out.println("Data Loader" + this.dataLoader);

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, T> inputStream = builder.stream(topicName, Consumed.with(Serdes.String(), consumerSerde));

        inputStream.print(Printed.toSysOut());

        inputStream.foreach((key, value) -> {
            dataLoader.add(value);
            System.out.println(value);
        });

        return builder.build();

    }

    public void stop() {
        logger.info("Stopping BSMNotificationTopology.");
        if (streams != null) {
            streams.close();
            streams.cleanUp();
            streams = null;
        }
        logger.info("Stopped BSMNotificationTopology.");
    }

    StateListener stateListener;
    public void registerStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
    }

    StreamsUncaughtExceptionHandler exceptionHandler;
    public void registerUncaughtExceptionHandler(StreamsUncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }



}
