package ru.practicum.shareit.exception;

public class IllegalBookingStatusException extends RuntimeException {
    public IllegalBookingStatusException(String message) {
        super(message);
    }
}
