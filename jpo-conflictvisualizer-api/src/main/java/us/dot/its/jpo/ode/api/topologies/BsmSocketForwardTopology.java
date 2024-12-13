package us.dot.its.jpo.ode.api.topologies;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.KafkaStreams.StateListener;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmIntersectionIdKey;
import us.dot.its.jpo.conflictmonitor.monitor.serialization.JsonSerdes;
import us.dot.its.jpo.ode.api.controllers.StompController;
import us.dot.its.jpo.ode.model.OdeBsmData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;

public class BsmSocketForwardTopology extends BaseTopology {

    protected final StompController controller;
    protected final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(BsmSocketForwardTopology.class);

    public BsmSocketForwardTopology(String topicName, StompController controller, Properties streamsProperties){
        super(topicName, streamsProperties);
        this.controller = controller;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<BsmIntersectionIdKey, OdeBsmData> inputStream = builder.stream(topicName, Consumed.with(JsonSerdes.BsmIntersectionIdKey(), JsonSerdes.OdeBsm()));

        inputStream.foreach(controller::broadcastBSM);

        return builder.build();

    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
