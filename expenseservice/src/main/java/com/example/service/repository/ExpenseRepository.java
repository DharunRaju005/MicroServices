package com.example.service.repository;


import com.example.service.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(String userId);

    List<Expense>findByUserIdAndCreatedAtBetween(String userId, Timestamp startTime,Timestamp endTime);

    Optional<Expense> findByUserIdAndExternalId(String userId, String externalId);

    Optional<Expense>findByExternalId(String externalId);

}

