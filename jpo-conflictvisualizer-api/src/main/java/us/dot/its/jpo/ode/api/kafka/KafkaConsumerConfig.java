package us.dot.its.jpo.ode.api.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmIntersectionIdKey;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes;
import us.dot.its.jpo.geojsonconverter.serialization.deserializers.JsonDeserializer;
import us.dot.its.jpo.geojsonconverter.serialization.deserializers.ProcessedMapDeserializer;
import us.dot.its.jpo.ode.model.OdeBsmData;

import java.util.HashMap;
import java.util.Map;

// Ref. https://github.com/eugenp/tutorials/blob/master/spring-kafka-3/src/main/java/com/baeldung/spring/kafka/startstopconsumer/KafkaConsumerConfig.java
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcessedSpat> spatListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProcessedSpat> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(spatConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcessedMap<LineString>> mapListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProcessedMap<LineString>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mapConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<BsmIntersectionIdKey, OdeBsmData> bsmListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<BsmIntersectionIdKey, OdeBsmData> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bsmConsumerFactory());
        return factory;
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, ProcessedSpat> spatConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, new StringDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, new JsonDeserializer<>(ProcessedSpat.class));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new JsonDeserializer<>(ProcessedSpat.class));
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, ProcessedMap<LineString>> mapConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, new StringDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, new ProcessedMapDeserializer<>(LineString.class));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new ProcessedMapDeserializer<>(LineString.class));
    }

    @Bean
    public DefaultKafkaConsumerFactory<BsmIntersectionIdKey, OdeBsmData> bsmConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, new JsonDeserializer<>(BsmIntersectionIdKey.class));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, new JsonDeserializer<>(OdeBsmData.class));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return new DefaultKafkaConsumerFactory<>(props,
                new JsonDeserializer<>(BsmIntersectionIdKey.class),
                new JsonDeserializer<>(OdeBsmData.class));
    }
}
