package ru.practicum.main.exception;

public class ValidationExceptionUserName extends RuntimeException {
    public ValidationExceptionUserName(String message) {
        super(message);
    }
}
