package us.dot.its.jpo.ode.api;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import us.dot.its.jpo.conflictmonitor.AlwaysContinueProductionExceptionHandler;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;
import java.util.Properties;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.SystemUtils;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.processor.LogAndSkipOnInvalidTimestamp;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.boot.info.BuildProperties;

@Configuration
@ConfigurationProperties("conflict.monitor.api")
public class ConflictMonitorApiProperties {

    @Getter
    @Setter
    private boolean kafkaConsumersAlwaysOn;

    private static int maximumResponseSize;
    private static String cors;
    private static final Logger logger = LoggerFactory.getLogger(ConflictMonitorApiProperties.class);

    private boolean confluentCloudEnabled = false;
    @Getter
    private String confluentKey = null;
    @Getter
    private String confluentSecret = null;

    private String version;
    private String kafkaBrokers = null;
    private static final String DEFAULT_KAFKA_PORT = "9092";
    private String cmServerURL = "";
    private String emailBroker = "";
    private String emailFromAddress = "noreply@cimms.com";
    private long mongoTimeoutMs = 5000;
    private String hostId;
    private List<Path> uploadLocations = new ArrayList<>();

    private String[] kafkaTopicsDisabled = {
            // disable all POJO topics by default except "topic.OdeBsmPojo". Never
            // "topic.OdeBsmPojo because that's the only way to get data into
            // "topic.OdeBsmJson
            "topic.OdeBsmRxPojo", "topic.OdeBsmTxPojo", "topic.OdeBsmDuringEventPojo", "topic.OdeTimBroadcastPojo" };
    private Set<String> kafkaTopicsDisabledSet = new HashSet<>();

    private String uploadLocationRoot = "uploads";
    private String uploadLocationObuLogLog = "bsmlog";

    /*
     * Security Services Module Properties
     */
    private String securitySvcsSignatureUri;
    private int securitySvcsPort = 8090;
    private String securitySvcsSignatureEndpoint = "sign";

    private int lingerMs = 0;

    private boolean enableAPI;
    private boolean enableEmails;
    private boolean enableReports;

    private BuildProperties buildProperties;

    @Getter
    @Setter
    private String connectURL = null;

    @Getter
    @Setter
    private String dockerHostIP = null;
    private static final String DEFAULT_CONNECT_PORT = "8083";

    @Autowired
    public ConflictMonitorApiProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public int getMaximumResponseSize() {
        return maximumResponseSize;
    }

    @Value("${maximumResponseSize}")
    public void setMaximumResponseSize(int maximumResponseSize) {
        ConflictMonitorApiProperties.maximumResponseSize = maximumResponseSize;
    }

    public String getCors() {
        return cors;
    }

    @Value("${cors}")
    public void setCors(String cors) {
        ConflictMonitorApiProperties.cors = cors;
    }

    /**
     * Returns the Conflict Monitor REST server URL. This URL is unauthenticated,
     * and only accessible internally.
     * 
     * @return
     */
    public String getCmServerURL() {
        return cmServerURL;
    }

    @Value("${cmServerURL}")
    public void setCmServerURL(String cmServerUrl) {
        this.cmServerURL = cmServerUrl;
    }

    public String getEmailBroker() {
        return emailBroker;
    }

    @Value("${emailBroker}")
    public void setEmailBroker(String emailBroker) {
        this.emailBroker = emailBroker;
    }

    public String getEmailFromAddress() {
        return emailFromAddress;
    }

    @Value("${emailFromAddress}")
    public void setEmailFromAddress(String emailFromAddress) {
        this.emailFromAddress = emailFromAddress;
    }

    public long getMongoTimeoutMs() {
        return mongoTimeoutMs;
    }

    @Value("${mongoTimeoutMs}")
    public void setMongoTimeoutMs(long mongoTimeoutMs) {
        this.mongoTimeoutMs = mongoTimeoutMs;
    }

    public String getKafkaBrokers() {
        return kafkaBrokers;
    }

    @Value("${spring.kafka.bootstrap-servers}")
    public void setKafkaBrokers(String kafkaBrokers) {
        this.kafkaBrokers = kafkaBrokers;
    }

    @Value("${kafka.linger_ms}")
    public void setKafkaLingerMs(int lingerMs) {
        this.lingerMs = lingerMs;
    }

    public int getKafkaLingerMs() {
        return lingerMs;
    }

    @Value("${enable.api}")
    public void setEnableApi(boolean enableApi) {
        this.enableAPI = enableApi;
    }

    public boolean isApiEnabled() {
        return enableAPI;
    }

    @Value("${enable.email}")
    public void setEnableEmail(boolean enableEmail) {
        this.enableAPI = enableEmail;
    }

    public boolean isEmailEnabled() {
        return enableEmails;
    }

    @Value("${enable.report}")
    public void setEnableReports(boolean enableReports) {
        this.enableReports = enableReports;
    }

    public boolean isReportsEnabled() {
        return enableReports;
    }

    public boolean getConfluentCloudEnabled() {
        return confluentCloudEnabled;
    }

    public void setConfluentCloudEnabled(boolean confluentCloudStatus) {
        this.confluentCloudEnabled = confluentCloudStatus;
    }

    public BuildProperties getBuildProperties() {
        return this.buildProperties;
    }

    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getSecuritySvcsPort() {
        return this.securitySvcsPort;
    }

    public void setSecuritySvcsPort(int securitySvcsPort) {
        this.securitySvcsPort = securitySvcsPort;
    }

    public String getSecuritySvcsSignatureEndpoint() {
        return this.securitySvcsSignatureEndpoint;
    }

    public void setSecuritySvcsSignatureEndpoint(String securitySvcsSignatureEndpoint) {
        this.securitySvcsSignatureEndpoint = securitySvcsSignatureEndpoint;
    }

    public String getUploadLocationObuLogLog() {
        return this.uploadLocationObuLogLog;
    }

    public void setUploadLocationObuLogLog(String uploadLocationObuLogLog) {
        this.uploadLocationObuLogLog = uploadLocationObuLogLog;
    }

    public String[] getKafkaTopicsDisabled() {
        return kafkaTopicsDisabled;
    }

    public void setKafkaTopicsDisabled(String[] kafkaTopicsDisabled) {
        this.kafkaTopicsDisabled = kafkaTopicsDisabled;
    }

    public Set<String> getKafkaTopicsDisabledSet() {
        return kafkaTopicsDisabledSet;
    }

    public void setKafkaTopicsDisabledSet(Set<String> kafkaTopicsDisabledSet) {
        this.kafkaTopicsDisabledSet = kafkaTopicsDisabledSet;
    }

    private static String getEnvironmentVariable(String variableName) {
        String value = System.getenv(variableName);
        if (value == null || value.equals("")) {
            System.out.println("Something went wrong retrieving the environment variable " + variableName);
        }
        return value;
    }

    @Bean
    public ObjectMapper defaultMapper() {
        ObjectMapper objectMapper = DateJsonMapper.getInstance();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        return objectMapper;
    }

    @PostConstruct
    void initialize() {
        setVersion(buildProperties.getVersion());
        logger.info("groupId: {}", buildProperties.getGroup());
        logger.info("artifactId: {}", buildProperties.getArtifact());
        logger.info("version: {}", version);

        uploadLocations.add(Paths.get(uploadLocationRoot));

        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // Let's just use a random hostname
            hostname = UUID.randomUUID().toString();
            logger.info("Unknown host error: {}, using random", e);
        }
        hostId = hostname;
        logger.info("Initializing services on host {}", hostId);

        if (kafkaBrokers == null) {

            String dockerIp = getEnvironmentVariable("DOCKER_HOST_IP");

            logger.info("ode.kafkaBrokers property not defined. Will try DOCKER_HOST_IP => {}", kafkaBrokers);

            if (dockerIp == null) {
                logger.warn(
                        "Neither ode.kafkaBrokers ode property nor DOCKER_HOST_IP environment variable are defined. Defaulting to localhost.");
                dockerIp = "localhost";
            }
            kafkaBrokers = dockerIp + ":" + DEFAULT_KAFKA_PORT;

            // URI for the security services /sign endpoint
            if (securitySvcsSignatureUri == null) {
                securitySvcsSignatureUri = "http://" + dockerIp + ":" + securitySvcsPort + "/"
                        + securitySvcsSignatureEndpoint;
            }
        }

        String kafkaType = getEnvironmentVariable("KAFKA_TYPE");
        if (kafkaType != null) {
            confluentCloudEnabled = kafkaType.equals("CONFLUENT");
            if (confluentCloudEnabled) {

                logger.info("Enabling Confluent Cloud Integration");

                confluentKey = getEnvironmentVariable("CONFLUENT_KEY");
                confluentSecret = getEnvironmentVariable("CONFLUENT_SECRET");
            }
        }

        // Initialize the Kafka Connect URL
        if (connectURL == null) {
            String dockerIp = getEnvironmentVariable("DOCKER_HOST_IP");
            if (dockerIp == null) {
                dockerIp = "localhost";
            }
            dockerHostIP = dockerIp;
            connectURL = String.format("http://%s:%s", dockerHostIP, DEFAULT_CONNECT_PORT);
        }

        List<String> asList = Arrays.asList(this.getKafkaTopicsDisabled());
        logger.info("Disabled Topics: {}", asList);
        kafkaTopicsDisabledSet.addAll(asList);
    }

    public Properties createStreamProperties(String name) {
        Properties streamProps = new Properties();
        streamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, name);

        streamProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);

        streamProps.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                LogAndContinueExceptionHandler.class.getName());

        streamProps.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG,
                LogAndSkipOnInvalidTimestamp.class.getName());

        streamProps.put(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG,
                AlwaysContinueProductionExceptionHandler.class.getName());

        streamProps.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);

        // streamProps.put(StreamsConfig.producerPrefix("acks"), "all");
        streamProps.put(StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG), "all");

        // Reduce cache buffering per topology to 1MB
        streamProps.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 1 * 1024 * 1024L);

        // Decrease default commit interval. Default for 'at least once' mode of 30000ms
        // is too slow.
        streamProps.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);

        streamProps.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // Configure the state store location
        if (SystemUtils.IS_OS_LINUX) {
            streamProps.put(StreamsConfig.STATE_DIR_CONFIG, "/var/lib/ode/kafka-streams");
        } else if (SystemUtils.IS_OS_WINDOWS) {
            streamProps.put(StreamsConfig.STATE_DIR_CONFIG, "C:/temp/ode");
        }

        // Increase max.block.ms and delivery.timeout.ms for streams
        final int FIVE_MINUTES_MS = 5 * 60 * 1000;
        streamProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, FIVE_MINUTES_MS);
        streamProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, FIVE_MINUTES_MS);

        streamProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "zstd");
        streamProps.put(ProducerConfig.LINGER_MS_CONFIG, getKafkaLingerMs());

        if (confluentCloudEnabled) {
            streamProps.put("ssl.endpoint.identification.algorithm", "https");
            streamProps.put("security.protocol", "SASL_SSL");
            streamProps.put("sasl.mechanism", "PLAIN");

            if (confluentKey != null && confluentSecret != null) {
                String auth = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + confluentKey + "\" " +
                        "password=\"" + confluentSecret + "\";";
                streamProps.put("sasl.jaas.config", auth);
            } else {
                logger.error(
                        "Environment variables CONFLUENT_KEY and CONFLUENT_SECRET are not set. Set these in the .env file to use Confluent Cloud");
            }
        }

        // Read from latest after restart
        // We do not want Kafka Streams default "earliest" for this app
        // https://docs.confluent.io/platform/current/streams/developer-guide/config-streams.html#default-values
        streamProps.setProperty(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "latest");
        // Restore and global consumers also set to latest, instead of streams default
        // "none"
        // which would cause exceptions to be thrown if no offset found.
        // Ref:
        // https://docs.confluent.io/platform/current/streams/developer-guide/config-streams.html#parameters-controlled-by-kstreams
        // https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html#auto-offset-reset
        streamProps.setProperty(StreamsConfig.restoreConsumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "latest");
        streamProps.setProperty(StreamsConfig.globalConsumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "latest");

        return streamProps;
    }

}
