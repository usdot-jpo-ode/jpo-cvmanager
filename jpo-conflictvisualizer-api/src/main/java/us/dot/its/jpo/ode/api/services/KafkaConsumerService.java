package us.dot.its.jpo.ode.api.services;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dot.its.jpo.conflictmonitor.monitor.models.config.Config;
import us.dot.its.jpo.ode.api.ConflictMonitorApiProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

@Service
public class KafkaConsumerService {

    @Autowired
    ConflictMonitorApiProperties props;

    public ArrayList<ConsumerRecord<String,Config>> getAllConfigsFromTopic(String topicName){
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getKafkaBrokers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "us.dot.its.jpo.ode.api.serialization.DefaultConfigDeserializer" + //
                "");
        
        Consumer<String, Config> consumer = new KafkaConsumer<>(consumerProps);

        // Find the partitions for the given topic
        for (PartitionInfo partitionInfo : consumer.partitionsFor(topicName)) {
            // Seek to the end of each partition
            consumer.assign(Collections.singleton(new org.apache.kafka.common.TopicPartition(topicName, partitionInfo.partition())));
            consumer.seekToBeginning(Collections.singleton(new org.apache.kafka.common.TopicPartition(topicName, partitionInfo.partition())));
        }

        ArrayList<ConsumerRecord<String, Config>> records = new ArrayList<>();
        for(ConsumerRecord<String, Config> record : consumer.poll(Duration.ofMillis(100))){
            records.add(record);
        }
        consumer.close();

        return records;
    }

    public Config getConfigFromTopic(String key, String topicName) {
        
        ArrayList<ConsumerRecord<String, Config>> records = getAllConfigsFromTopic(topicName);

        for(ConsumerRecord<String, Config> record : records){
            if(record.value().getKey().equals(key)){
                return record.value();
            }
        }
        return null;
    }




    public void deleteConfigFromTopic(){

    }
}