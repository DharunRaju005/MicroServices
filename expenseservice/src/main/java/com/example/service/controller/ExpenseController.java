package com.example.service.controller;

import com.example.service.dto.ExpenseDto;
import com.example.service.entity.Expense;
import com.example.service.repository.ExpenseRepository;
import com.example.service.service.ExpenseService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expense/v1")
public class ExpenseController {
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseRepository expenseRepository, ExpenseService expenseService) {
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
    }

    @GetMapping(path = "/getExpense")
    public ResponseEntity<List<ExpenseDto>> getExpense(@RequestParam(value = "user_id")@NonNull String userId) {
        try{
            List<ExpenseDto> expenseDtos = expenseService.getExpenses(userId);
            return new ResponseEntity<>(expenseDtos, HttpStatus.OK);
        }catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/addExpense")
    public ResponseEntity<Boolean> addExpense(@RequestHeader(value = "X-User-Id") @NonNull String userId,@RequestBody ExpenseDto expenseDto) {
        try {
            expenseDto.setUserId(userId);
            expenseService.createExpense(expenseDto);
            return new ResponseEntity<>(true, HttpStatus.CREATED);
//            return ResponseEntity.status(HttpStatus.CREATED).body(true);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(false,HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(path = "/updateExpense")
    public ResponseEntity<Boolean>updadteExpense(@RequestHeader(value = "X-User-Id") @NonNull String userId,@RequestBody ExpenseDto expenseDto) {
        try{
            Boolean updationStatus = expenseService.updateExpense(expenseDto);
            if(updationStatus) {
                return new ResponseEntity<>(true, HttpStatus.OK);
            }
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(false,HttpStatus.BAD_REQUEST);
        }
    }
}
