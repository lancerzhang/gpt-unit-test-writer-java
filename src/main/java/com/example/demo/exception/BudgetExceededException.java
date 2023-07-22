package com.example.demo.exception;

public class BudgetExceededException extends RuntimeException {
    public BudgetExceededException(String message) {
        super(message);
    }
}
