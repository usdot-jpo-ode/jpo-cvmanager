// package us.dot.its.jpo.ode.api.services;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.stereotype.Service;

// @Service
// public class KafkaProducerService {

//     private final KafkaTemplate<String, String> kafkaTemplate;

//     @Autowired
//     public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
//         this.kafkaTemplate = kafkaTemplate;
//     }

//     public void sendMessage(String topic, String message) {
//         kafkaTemplate.send(topic, message);
//         System.out.println("Message sent to topic: " + topic);
//     }
// }