package us.dot.its.jpo.ode.api.topologies;

/**
 * Interface for a Kafka Streams topology that can be started and stopped
 */
public interface RestartableTopology {
    void start();
    void stop();
}
