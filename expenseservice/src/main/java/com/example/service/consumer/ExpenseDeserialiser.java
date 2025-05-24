package com.example.service.consumer;

import com.example.service.dto.ExpenseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class ExpenseDeserialiser implements Deserializer<ExpenseDto> {

    @Override
    public ExpenseDto deserialize(String arg0, byte[] arg1) {
         ObjectMapper mapper = new ObjectMapper();
         ExpenseDto expenseDto = null;
         try {
             expenseDto=mapper.readValue(arg1, ExpenseDto.class);
         }catch (Exception e){
             e.printStackTrace();
         }
         return expenseDto;
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> configs, boolean isKey){}
}
