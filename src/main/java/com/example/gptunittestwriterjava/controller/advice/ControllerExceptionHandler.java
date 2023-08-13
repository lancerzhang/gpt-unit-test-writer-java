package com.example.gptunittestwriterjava.controller.advice;

import com.example.gptunittestwriterjava.exception.InsufficientBudgetException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(InsufficientBudgetException.class)
    public ResponseEntity<String> handleInsufficientBudgetException(InsufficientBudgetException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);  // You can use another suitable status code.
    }

    // ... handle other exceptions ...
}
