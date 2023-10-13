package ru.practicum.main.exception;

public class RepeatParticipationRequestException extends RuntimeException {
    public RepeatParticipationRequestException(String message) {
        super(message);
    }
}