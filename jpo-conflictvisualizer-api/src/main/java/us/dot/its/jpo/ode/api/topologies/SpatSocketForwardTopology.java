package us.dot.its.jpo.ode.api.topologies;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes;
import us.dot.its.jpo.ode.api.controllers.StompController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;

public class SpatSocketForwardTopology extends BaseTopology {

    private static final Logger logger = LoggerFactory.getLogger(SpatSocketForwardTopology.class);

    StompController controller;

    public SpatSocketForwardTopology(String topicName, StompController controller, Properties streamsProperties) {
        super(topicName, streamsProperties);
        this.controller = controller;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, ProcessedSpat> inputStream = builder.stream(topicName, Consumed.with(Serdes.String(), JsonSerdes.ProcessedSpat()));

        inputStream.foreach((key, value) -> {
            controller.broadcastSpat(value);
        });

        return builder.build();

    }

}
