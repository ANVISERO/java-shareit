package ru.practicum.shareit.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.exception.IncorrectDataException;
import ru.practicum.shareit.exception.NotFoundException;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException exception) {
        log.warn("Искомый объект не найден. {}", exception.getMessage());
        return new ErrorResponse("Искомый объект не найден", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(final ConstraintViolationException exception) {
        log.warn("Ошибка валидации. {}", exception.getMessage());
        return new ErrorResponse("Ошибка валидации", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIncorrectDataException(final IncorrectDataException exception) {
        log.warn("Переданы некорректные данные. {}", exception.getMessage());
        return new ErrorResponse("Переданы некорректные данные", exception.getMessage());
    }
}
