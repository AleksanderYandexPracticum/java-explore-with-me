package ru.practicum.main.exception;

public class ValidationExceptionEvent extends RuntimeException {
    public ValidationExceptionEvent(String message) {
        super(message);
    }
}