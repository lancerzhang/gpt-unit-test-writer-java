package com.example.gptunittestwriterjava.exception;

public class ExistingJobException extends RuntimeException {
    public ExistingJobException(String message) {
        super(message);
    }
}
