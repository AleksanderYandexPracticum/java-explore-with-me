package ru.practicum.main.exception;

public class DuplicateNameException extends RuntimeException {
    public DuplicateNameException(String message) {
        super(message);
    }
}

