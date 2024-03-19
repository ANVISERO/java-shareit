package ru.practicum.shareit.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.*;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException exception) {
        log.warn("Object not found. {}", exception.getMessage());
        return new ErrorResponse("Object not found", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(final ConstraintViolationException exception) {
        log.warn("Validation error. {}", exception.getMessage());
        return new ErrorResponse("Validation error", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePermissionException(final PermissionException exception) {
        log.warn("Permission denied. {}", exception.getMessage());
        return new ErrorResponse("Permission denied", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException exception) {
        log.warn("Illegal argument. {}", exception.getMessage());
        return new ErrorResponse("Illegal argument", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleAlreadyBookedException(final AlreadyBookedException exception) {
        log.warn("This item is already booked. {}", exception.getMessage());
        return new ErrorResponse("This item is already booked", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleAccessToAddCommentDeniedException(final AccessToAddCommentDeniedException exception) {
        log.warn("Permission to write comments denied. {}", exception.getMessage());
        return new ErrorResponse("Permission to write comments denied", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleChangeStatusException(final ChangeStatusException exception) {
        log.warn("Can't change booking status. {}", exception.getMessage());
        return new ErrorResponse("Can't change booking status", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalBookingStatusException(final IllegalBookingStatusException exception) {
        String[] parts = exception.getMessage().split("\\.", 2);
        String firstPart = parts[0];
        String secondPart = parts.length > 1 ? parts[1].substring(1) : "";
        log.warn("Unknown state: {}", exception.getMessage());
        return new ErrorResponse("Unknown state: " + firstPart, secondPart);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIncorrectDataException(final IncorrectDataException exception) {
        log.warn("Incorrect parameters were transmitted. {}", exception.getMessage());
        return new ErrorResponse("Incorrect parameters were transmitted", exception.getMessage());
    }
}
