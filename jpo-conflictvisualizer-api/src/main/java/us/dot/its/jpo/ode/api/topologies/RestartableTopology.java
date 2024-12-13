package us.dot.its.jpo.ode.api.topologies;

/**
 * Interface for a Kafka Streams topology that can be stopped and restarted
 */
public interface RestartableTopology {
    void start();
    void stop();
    String getTopicName();
}
