package us.dot.its.jpo.ode.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmIntersectionIdKey;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.geojsonconverter.serialization.deserializers.JsonDeserializer;
import us.dot.its.jpo.geojsonconverter.serialization.deserializers.ProcessedBsmDeserializer;
import us.dot.its.jpo.geojsonconverter.serialization.deserializers.ProcessedMapDeserializer;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Ref. https://github.com/eugenp/tutorials/blob/master/spring-kafka-3/src/main/java/com/baeldung/spring/kafka/startstopconsumer/KafkaConsumerConfig.java
@EnableKafka
@Configuration
@Slf4j
@ConditionalOnProperty(name = "enable.api", havingValue = "true", matchIfMissing = false)
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ConflictMonitorApiProperties properties;

    public KafkaConsumerConfig(ConflictMonitorApiProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcessedSpat> spatListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProcessedSpat> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(spatConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcessedMap<LineString>> mapListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProcessedMap<LineString>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mapConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<BsmIntersectionIdKey, ProcessedBsm<Point>> bsmListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<BsmIntersectionIdKey, ProcessedBsm<Point>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bsmConsumerFactory());
        return factory;
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, ProcessedSpat> spatConsumerFactory() {
        return consumerFactory(ListenerIds.SPAT,
                new StringDeserializer(),
                new JsonDeserializer<>(ProcessedSpat.class));
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, ProcessedMap<LineString>> mapConsumerFactory() {
        return consumerFactory(ListenerIds.MAP,
                new StringDeserializer(),
                new ProcessedMapDeserializer<>(LineString.class));
    }

    @Bean
    public DefaultKafkaConsumerFactory<BsmIntersectionIdKey, ProcessedBsm<Point>> bsmConsumerFactory() {
        return consumerFactory(ListenerIds.BSM,
                new JsonDeserializer<>(BsmIntersectionIdKey.class),
                new ProcessedBsmDeserializer<>(Point.class));
    }

    private <TKey, TValue> DefaultKafkaConsumerFactory<TKey, TValue> consumerFactory(
            String id, Deserializer<TKey> keyDeserializer, Deserializer<TValue> valueDeserializer) {
        Map<String, Object> props = commonProps(id);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        return new DefaultKafkaConsumerFactory<TKey, TValue>(props,
                keyDeserializer,
                valueDeserializer);
    }

    private Map<String, Object> commonProps(final String id) {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.CLIENT_ID_CONFIG, id);

        // Make the consumer group id be unique to the instance to prevent consumers
        // forming into groups
        // if running more than one instance
        String groupIdSuffix;
        try {
            groupIdSuffix = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("Unable to find host IP address, using random group id");
            groupIdSuffix = UUID.randomUUID().toString();
        }
        final String groupId = id + "_" + groupIdSuffix;
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        log.info("Consumer group ID = {}", groupId);

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100);

        if (properties.getConfluentCloudEnabled()) {
            props.put("ssl.endpoint.identification.algorithm", "https");
            props.put("security.protocol", "SASL_SSL");
            props.put("sasl.mechanism", "PLAIN");

            if (properties.getConfluentKey() != null && properties.getConfluentSecret() != null) {
                String auth = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + properties.getConfluentKey() + "\" " +
                        "password=\"" + properties.getConfluentSecret() + "\";";
                props.put("sasl.jaas.config", auth);
            } else {
                log.error(
                        "Environment variables CONFLUENT_KEY and CONFLUENT_SECRET are not set. Set these in the .env file to use Confluent Cloud");
            }
        }
        return props;
    }
}
