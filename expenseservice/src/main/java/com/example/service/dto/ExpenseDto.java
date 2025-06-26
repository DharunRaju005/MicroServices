package com.example.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class   ExpenseDto {
     private String externalId;

     @JsonProperty(value = "amount")
     @NonNull
     private String amount;

     @JsonProperty(value = "user_id")
     private String userId;

     @JsonProperty(value = "merchant")
     private String merchant;

     @JsonProperty(value = "currency")
     private String currency;

     @JsonProperty(value = "created_at")
     private Timestamp createdAt;

     private BigDecimal amountValue;

     public ExpenseDto(String json){
          try{
               ObjectMapper objectMapper=new ObjectMapper();
               objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
               ExpenseDto expense= objectMapper.readValue(json,ExpenseDto.class);
               this.externalId=expense.externalId;
               this.amount=expense.amount;
               this.userId=expense.userId;
               this.merchant=expense.merchant;
               this.currency=expense.currency;
               this.createdAt=expense.createdAt;
          }catch(Exception e) {
               throw new RuntimeException("Failed to deserilse expensedto from json", e);
          }
     }

}
