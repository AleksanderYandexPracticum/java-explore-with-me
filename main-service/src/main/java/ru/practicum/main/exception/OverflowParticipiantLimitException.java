package ru.practicum.main.exception;

public class OverflowParticipiantLimitException extends RuntimeException {
    public OverflowParticipiantLimitException(String message) {
        super(message);
    }
}