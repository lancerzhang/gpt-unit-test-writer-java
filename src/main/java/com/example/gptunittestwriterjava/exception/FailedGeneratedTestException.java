package com.example.gptunittestwriterjava.exception;

public class FailedGeneratedTestException extends RuntimeException {
    public FailedGeneratedTestException(String message) {
        super(message);
    }
}
