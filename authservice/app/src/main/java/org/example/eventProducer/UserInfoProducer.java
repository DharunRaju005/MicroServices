package org.example.eventProducer;

import org.example.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class UserInfoProducer {

    private final KafkaTemplate<String, UserDto> kafkaTemplate;

    @Value("${spring.kafka.topic-json.name}")
    private String topicJsonName;

    @Autowired
    UserInfoProducer(KafkaTemplate<String,UserDto>kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEventToKafka(UserInfoEvent eventData){
        Message<UserInfoEvent> message = MessageBuilder.withPayload(eventData).setHeader(KafkaHeaders.TOPIC,topicJsonName).build();
        kafkaTemplate.send(message);
    }


}
