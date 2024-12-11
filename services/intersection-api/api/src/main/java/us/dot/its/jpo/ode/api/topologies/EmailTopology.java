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

import us.dot.its.jpo.ode.api.models.DataLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Properties;



public class EmailTopology<T>{

    private static final Logger logger = LoggerFactory.getLogger(DataLoaderTopology.class);

    Topology topology;
    KafkaStreams streams;
    String topicName;
    Serde<T> consumerSerde;
    DataLoader<T> dataLoader;
    Properties streamsProperties;

    public EmailTopology(String topicName, Serde<T> consumerSerde, DataLoader<T> dataLoader, Properties streamsProperties){
        this.topicName = topicName;
        this.consumerSerde = consumerSerde;
        this.dataLoader = dataLoader;
        this.streamsProperties = streamsProperties;
        this.start();
    }

    
    public void start() {
        if (streams != null && streams.state().isRunningOrRebalancing()) {
            throw new IllegalStateException("Start called while streams is already running.");
        }
        Topology topology = buildTopology();
        streams = new KafkaStreams(topology, streamsProperties);
        if (exceptionHandler != null) streams.setUncaughtExceptionHandler(exceptionHandler);
        if (stateListener != null) streams.setStateListener(stateListener);
        streams.start();
    }

    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, T> inputStream = builder.stream(topicName, Consumed.with(Serdes.String(), consumerSerde));

        inputStream.foreach((key, value) -> {
            dataLoader.add(value);
        });

        return builder.build();

    }

    public void stop() {
        logger.info("Stopping Data Loading Topology.");
        if (streams != null) {
            streams.close();
            streams.cleanUp();
            streams = null;
        }
        logger.info("Stopped Data Loading Topology.");
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
