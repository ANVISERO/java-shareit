package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingClient bookingClient;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<Object> createBooking(
            @Validated({OnCreate.class, Default.class}) @RequestBody BookingDto bookingDto,
            @Positive @RequestHeader(value = USER_HEADER) Long bookerId) {
        log.debug("POST request received to create a new booking {} of the item with id = {} by user with id = {}",
                bookingDto, bookingDto.getItemId(), bookerId);
        return bookingClient.createBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> changeBookingStatus(@PathVariable(name = "bookingId") @Positive Long bookingId,
                                                      @RequestParam(name = "approved") Boolean approved,
                                                      @RequestHeader(value = USER_HEADER) @Positive Long ownerId) {
        log.debug("PATCH request received to {} booking by id = {} from user with id = {}",
                approved ? "approve" : "disapprove", bookingId, ownerId);
        return bookingClient.changeBookingStatus(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@PathVariable(name = "bookingId") @Positive Long bookingId,
                                                 @RequestHeader(value = USER_HEADER) @Positive Long userId) {
        log.debug("GET request received to get booking by id = {} by user with id = {}", bookingId, userId);
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserBookingsByStatus(
            @RequestParam(name = "state", required = false, defaultValue = "ALL") BookingStatus bookingStatus,
            @RequestHeader(value = USER_HEADER) @Positive Long userId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.debug("GET request received to get all bookings by user with id = {} and with state = {}", userId,
                bookingStatus);
        return bookingClient.getAllUserBookingsByStatus(bookingStatus, userId, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllOwnerItemsBookingsByStatus(
            @RequestParam(name = "state", required = false, defaultValue = "ALL") BookingStatus bookingStatus,
            @RequestHeader(value = USER_HEADER) @Positive Long ownerId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.debug("GET request received to get all bookings by owner with id = {} and with state = {}", ownerId,
                bookingStatus);
        return bookingClient.getAllOwnerItemsBookingsByStatus(bookingStatus, ownerId, from, size);
    }
}
