//package us.dot.its.jpo.ode.api.topologies;
//
//
//import org.apache.kafka.common.serialization.Serde;
//import org.apache.kafka.common.serialization.Serdes;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.Topology;
//import org.apache.kafka.streams.kstream.Consumed;
//import org.apache.kafka.streams.kstream.KStream;
//
//import us.dot.its.jpo.ode.api.models.DataLoader;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
//import java.util.Properties;
//
//
//
//public class EmailTopology<T> extends BaseTopology {
//
//    private static final Logger logger = LoggerFactory.getLogger(EmailTopology.class);
//
//
//
//    Serde<T> consumerSerde;
//    DataLoader<T> dataLoader;
//
//
//    public EmailTopology(String topicName, Serde<T> consumerSerde, DataLoader<T> dataLoader, Properties streamsProperties){
//        super(topicName, streamsProperties);
//        this.consumerSerde = consumerSerde;
//        this.dataLoader = dataLoader;
//    }
//
//    @Override
//    protected Logger getLogger() {
//        return logger;
//    }
//
//    public Topology buildTopology() {
//        StreamsBuilder builder = new StreamsBuilder();
//
//        KStream<String, T> inputStream = builder.stream(topicName, Consumed.with(Serdes.String(), consumerSerde));
//
//        inputStream.foreach((key, value) -> {
//            dataLoader.add(value);
//        });
//
//        return builder.build();
//
//    }
//
//
//}
