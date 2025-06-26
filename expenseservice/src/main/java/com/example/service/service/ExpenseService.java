package com.example.service.service;


import com.example.service.dto.ExpenseDto;
import com.example.service.entity.Expense;
import com.example.service.repository.ExpenseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ExpenseService {
    private ExpenseRepository expenseRepository;
    private ObjectMapper objectMapper=new ObjectMapper();


    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public boolean createExpense(ExpenseDto expenseDto) {
        //this is to make sure of idempotency
        //------------X--------
        Optional<Expense> expense = expenseRepository.findByExternalId(expenseDto.getExternalId());
        if (expense.isPresent()) {
            return false;
        }
        //------------X--------
        setCurrency(expenseDto);
        try{
            expenseRepository.save(objectMapper.convertValue(expenseDto, Expense.class));
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean updateExpense(ExpenseDto expenseDto,String userId) {
        setCurrency(expenseDto);
        Optional<Expense> expenseOptional=expenseRepository.findByUserIdAndExternalId(userId,expenseDto.getExternalId());
        if(expenseOptional.isEmpty()){
            return false;
        }
        Expense expense=expenseOptional.get();
        expense.setAmount(expenseDto.getAmount());
        expense.setCurrency(expenseDto.getCurrency());
        expense.setMerchant(Strings.isNotBlank(expenseDto.getMerchant())?expenseDto.getMerchant():expense.getMerchant());
        expense.setCurrency(Strings.isNotBlank(expenseDto.getCurrency())?expenseDto.getCurrency():expense.getCurrency());
        expenseRepository.save(expense);
        return true;
    }

    public List<ExpenseDto>getExpenses(String userId){
        List<Expense> expenseList=expenseRepository.findByUserId(userId);
        //converting entity to dto
        return objectMapper.convertValue(expenseList, new TypeReference<List<ExpenseDto>>() {});

    }

    private void setCurrency(ExpenseDto expenseDto) {
        if(Objects.isNull(expenseDto.getCurrency())){
            expenseDto.setCurrency("INR");
        }
    }
}
