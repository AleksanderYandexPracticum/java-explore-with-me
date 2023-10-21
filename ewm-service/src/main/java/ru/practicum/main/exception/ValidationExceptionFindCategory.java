package ru.practicum.main.exception;

public class ValidationExceptionFindCategory extends RuntimeException {
    public ValidationExceptionFindCategory(String message) {
        super(message);
    }
}