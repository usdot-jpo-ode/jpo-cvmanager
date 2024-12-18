package us.dot.its.jpo.ode.api.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.geojsonconverter.serialization.deserializers.JsonDeserializer;

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
}
