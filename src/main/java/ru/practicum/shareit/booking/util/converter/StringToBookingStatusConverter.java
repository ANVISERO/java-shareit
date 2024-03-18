package ru.practicum.shareit.booking.util.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.IllegalBookingStatusException;

@Slf4j
public class StringToBookingStatusConverter implements Converter<String, BookingStatus> {
    @Override
    public BookingStatus convert(String source) {
        log.debug("converter {}", source);
        BookingStatus bookingStatus;
        try {
            bookingStatus = BookingStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalBookingStatusException(source + ". " + e.getMessage());
        }
        return bookingStatus;
    }
}
