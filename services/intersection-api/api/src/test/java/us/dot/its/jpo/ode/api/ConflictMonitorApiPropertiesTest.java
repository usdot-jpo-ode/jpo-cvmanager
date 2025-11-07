package us.dot.its.jpo.ode.api;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.processor.LogAndSkipOnInvalidTimestamp;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import us.dot.its.jpo.conflictmonitor.AlwaysContinueProductionExceptionHandler;

import org.apache.commons.lang3.SystemUtils;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ConflictMonitorApiPropertiesTest {

    @Test
    void testCreateStreamProperties_Defaults() {
        ConflictMonitorApiProperties props = new ConflictMonitorApiProperties(null);
        props.setKafkaBrokers("localhost:9092");
        props.setKafkaLingerMs(123);
        props.setConfluentCloudEnabled(false);

        Properties streamProps = props.createStreamProperties("test-app");

        assertThat(streamProps.getProperty(StreamsConfig.APPLICATION_ID_CONFIG)).isEqualTo("test-app");
        assertThat(streamProps.getProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(streamProps.getProperty(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG))
                .isEqualTo(LogAndContinueExceptionHandler.class.getName());
        assertThat(streamProps.getProperty(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG))
                .isEqualTo(LogAndSkipOnInvalidTimestamp.class.getName());
        assertThat(streamProps.getProperty(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG))
                .isEqualTo(AlwaysContinueProductionExceptionHandler.class.getName());
        assertThat(streamProps.get(StreamsConfig.NUM_STREAM_THREADS_CONFIG)).isEqualTo(2);
        assertThat(streamProps.getProperty(StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG)))
                .isEqualTo("all");
        assertThat(streamProps.get(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG)).isEqualTo(1048576L);
        assertThat(streamProps.get(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG)).isEqualTo(100);
        assertThat(streamProps.getProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG))
                .isEqualTo(Serdes.String().getClass().getName());
        assertThat(streamProps.get(ProducerConfig.MAX_BLOCK_MS_CONFIG)).isEqualTo(300000);
        assertThat(streamProps.get(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG)).isEqualTo(300000);
        assertThat(streamProps.getProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG)).isEqualTo("zstd");
        assertThat(streamProps.get(ProducerConfig.LINGER_MS_CONFIG)).isEqualTo(123);
        assertThat(streamProps.getProperty(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)))
                .isEqualTo("latest");
        assertThat(
                streamProps.getProperty(StreamsConfig.restoreConsumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)))
                .isEqualTo("latest");
        assertThat(streamProps.getProperty(StreamsConfig.globalConsumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)))
                .isEqualTo("latest");

        // State dir depends on OS
        if (SystemUtils.IS_OS_LINUX) {
            assertThat(streamProps.getProperty(StreamsConfig.STATE_DIR_CONFIG)).isEqualTo("/var/lib/ode/kafka-streams");
        } else if (SystemUtils.IS_OS_WINDOWS) {
            assertThat(streamProps.getProperty(StreamsConfig.STATE_DIR_CONFIG)).isEqualTo("C:/temp/ode");
        }
    }

    @Test
    void testCreateStreamProperties_ConfluentCloud() {
        ConflictMonitorApiProperties realProps = new ConflictMonitorApiProperties(null);
        ConflictMonitorApiProperties props = Mockito.spy(realProps);

        props.setKafkaBrokers("cloud:9092");
        props.setKafkaLingerMs(456);
        props.setConfluentCloudEnabled(true);

        when(props.getConfluentKey()).thenReturn("key");
        when(props.getConfluentSecret()).thenReturn("secret");

        Properties streamProps = props.createStreamProperties("cloud-app");

        assertThat(streamProps.getProperty("ssl.endpoint.identification.algorithm")).isEqualTo("https");
        assertThat(streamProps.getProperty("security.protocol")).isEqualTo("SASL_SSL");
        assertThat(streamProps.getProperty("sasl.mechanism")).isEqualTo("PLAIN");
        assertThat(streamProps.getProperty("sasl.jaas.config"))
                .contains("username=\"key\"")
                .contains("password=\"secret\"");
    }
}