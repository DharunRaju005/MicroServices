package org.example.serialiser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.example.eventProducer.UserInfoEvent;
import org.example.model.UserDto;

import java.util.Map;

public class UserInfoSerialiser implements Serializer<UserInfoEvent> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String arg0, UserInfoEvent arg1) {
        byte[] retVal = null;
        ObjectMapper mapper = new ObjectMapper();
        try{
            retVal=mapper.writeValueAsString(arg1).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }


    @Override
    public void close() {

    }
}
