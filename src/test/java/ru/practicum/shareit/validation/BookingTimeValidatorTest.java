package ru.practicum.shareit.validation;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookingTimeValidatorTest {

    private BookingTimeValidator bookingTimeValidator;
    private BookingDto booking;

    @BeforeEach
    public void setUp() {
        bookingTimeValidator = new BookingTimeValidator();
        booking = BookingDto.builder().build();
    }

    @Test
    public void shouldReturnTrueWhenStartIsBeforeEnd() {
        booking.setStart(LocalDateTime.of(2024, 4, 6, 10, 0));
        booking.setEnd(LocalDateTime.of(2024, 4, 6, 12, 0));

        boolean isValid = bookingTimeValidator.isValid(booking, null);

        assertTrue(isValid);
    }

    @Test
    public void shouldReturnFalseWhenStartIsAfterEnd() {
        booking.setStart(LocalDateTime.of(2024, 4, 6, 12, 0));
        booking.setEnd(LocalDateTime.of(2024, 4, 6, 10, 0));

        boolean isValid = bookingTimeValidator.isValid(booking, null);

        assertFalse(isValid);
    }

    @Test
    public void shouldReturnFalseWhenStartOrEndIsNull() {
        booking.setStart(null);
        booking.setEnd(LocalDateTime.of(2024, 4, 6, 12, 0));

        boolean isValid = bookingTimeValidator.isValid(booking, null);

        assertFalse(isValid);
    }
}
