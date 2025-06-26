package com.example.service.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Expense {

    @Id
    @Column(name="id")

    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name="external_id")
    private String externalId;

    @JsonProperty("user_id")
    @Column(name="user_id")
    private String userId;

    @Column(name ="amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name="merchant")
    private String merchant;

    @PrePersist
    @PreUpdate
    private void generateExternalId() {
        if(this.externalId==null){
            this.externalId = UUID.randomUUID().toString();
        }
        if(this.createdAt==null){
            this.createdAt = new Timestamp(Instant.now().toEpochMilli());
        }
    }



}
