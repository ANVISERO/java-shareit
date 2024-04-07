package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.util.mapper.BookingMapper;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final String userHeader = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto createBooking(@Validated({OnCreate.class, Default.class}) @RequestBody BookingDto bookingDto,
                                    @Positive @RequestHeader(value = userHeader) Long bookerId) {
        log.debug("POST request received to create a new booking {} of the item with id = {} by user with id = {}",
                bookingDto, bookingDto.getItemId(), bookerId);
        Booking booking = bookingMapper.bookingDtoToBooking(bookingDto);
        log.debug("The message body converted to an object {}", booking);
        return bookingService.createBooking(booking, bookerId, bookingDto.getItemId());
    }

    @PatchMapping("/{bookingId}")
    public BookingDto changeBookingStatus(@PathVariable(name = "bookingId") @Positive Long bookingId,
                                          @RequestParam(name = "approved") Boolean approved,
                                          @RequestHeader(value = userHeader) @Positive Long ownerId) {
        log.debug("PATCH request received to {} booking by id = {} from user with id = {}",
                approved ? "approve" : "disapprove", bookingId, ownerId);
        return bookingService.changeBookingStatus(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable(name = "bookingId") @Positive Long bookingId,
                                     @RequestHeader(value = userHeader) @Positive Long userId) {
        log.debug("GET request received to get booking by id = {} by user with id = {}", bookingId, userId);
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllUserBookingsByStatus(
            @RequestParam(name = "state", required = false, defaultValue = "ALL") BookingStatus bookingStatus,
            @RequestHeader(value = userHeader) @Positive Long userId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.debug("GET request received to get all bookings by user with id = {} and with state = {}", userId,
                bookingStatus);
        return bookingService.getAllUserBookingsByStatus(bookingStatus, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllOwnerItemsBookingsByStatus(
            @RequestParam(name = "state", required = false, defaultValue = "ALL") BookingStatus bookingStatus,
            @RequestHeader(value = userHeader) @Positive Long ownerId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.debug("GET request received to get all bookings by owner with id = {} and with state = {}", ownerId,
                bookingStatus);
        return bookingService.getAllOwnerItemsBookingsByStatus(bookingStatus, ownerId, from, size);
    }
}
