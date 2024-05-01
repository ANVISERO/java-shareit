package ru.practicum.shareit.exception;

public class AccessToAddCommentDeniedException extends RuntimeException {
    public AccessToAddCommentDeniedException(String message) {
        super(message);
    }
}
