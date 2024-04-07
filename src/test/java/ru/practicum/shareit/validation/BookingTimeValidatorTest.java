package ru.practicum.shareit.validation;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookingTimeValidatorTest {

    private BookingTimeValidator bookingTimeValidator;
    private BookingDto booking;
    LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    public void setUp() {
        bookingTimeValidator = new BookingTimeValidator();
        booking = BookingDto.builder().build();
    }

    @Test
    @DisplayName("isValid_whenStartIsBeforeEnd_thenTrueReturned")
    public void isValid_whenStartIsBeforeEnd_thenTrueReturned() {
        booking.setStart(now);
        booking.setEnd(now.plusDays(1));

        boolean isValid = bookingTimeValidator.isValid(booking, null);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("isValid_whenStartIsAfterEnd_thenFalseReturned")
    public void isValid_whenStartIsAfterEnd_thenFalseReturned() {
        booking.setStart(now.plusDays(1));
        booking.setEnd(now);

        boolean isValid = bookingTimeValidator.isValid(booking, null);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("isValid_whenStartIsNull_thenFalseReturned")
    public void isValid_whenStartIsNull_thenFalseReturned() {
        booking.setStart(null);
        booking.setEnd(now);

        boolean isValid = bookingTimeValidator.isValid(booking, null);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("isValid_whenEndIsNull_thenFalseReturned")
    public void isValid_whenEndIsNull_thenFalseReturned() {
        booking.setStart(now);
        booking.setEnd(null);

        boolean isValid = bookingTimeValidator.isValid(booking, null);

        assertFalse(isValid);
    }
}
