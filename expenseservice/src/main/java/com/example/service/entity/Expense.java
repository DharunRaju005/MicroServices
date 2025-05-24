package com.example.service.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Expense {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name="external_id")
    private String externalId;

    @Column(name="user_id")
    private String userId;

    @Column(name ="amount")
    private String amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name="merchant")
    private String merchant;

    @PrePersist
    @PreUpdate
    private void generateExternalId() {
        this.externalId = UUID.randomUUID().toString();
    }



}
