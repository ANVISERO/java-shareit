package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(Booking booking, Long bookerId, Long itemId);

    BookingDto changeBookingStatus(Long bookingId, Boolean approved, Long bookerId);

    BookingDto getBookingById(Long bookingId, Long userId);

    List<BookingDto> getAllUserBookingsByStatus(BookingStatus bookingStatus, Long userId, int from, int size);

    List<BookingDto> getAllOwnerItemsBookingsByStatus(BookingStatus bookingStatus, Long ownerId, int from, int size);
}
