package ru.practicum.shareit.validation;

import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BookingTimeValidator implements ConstraintValidator<StartBeforeEnd, BookingDto> {
    @Override
    public boolean isValid(BookingDto booking, ConstraintValidatorContext context) {
        return booking.getStart() != null && booking.getEnd() != null && booking.getEnd().isAfter(booking.getStart());
    }
}
