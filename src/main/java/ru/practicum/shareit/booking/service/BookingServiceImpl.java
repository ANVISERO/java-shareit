package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.util.mapper.BookingMapper;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.ShareItPageRequest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDto createBooking(Booking booking, Long bookerId, Long itemId) {
        User booker = userRepository.findById(bookerId).orElseThrow(() -> {
            log.warn("User with id = {} not found", bookerId);
            return new NotFoundException(String.format("User with id = %d not found", bookerId));
        });
        log.debug("User with id = {} found", bookerId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id = {} not found", itemId);
            return new NotFoundException(String.format("Item with id = %d not found", itemId));
        });
        log.debug("Item with id = {} found", itemId);
        if (Objects.equals(booker.getId(), item.getOwner().getId())) {
            log.warn("User with id = {} can't book his own item", bookerId);
            throw new PermissionException(String.format("User with id = %d can't book his own item", bookerId));
        }
        if (!item.getAvailable() || booking.getEnd().isBefore(booking.getStart())
                || booking.getEnd().equals(booking.getStart())) {
            log.warn("Item with id = {} can not be booked", itemId);
            throw new AlreadyBookedException(String.format("Item with id = %d can not be booked", itemId));
        }
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(booker);
        booking.setItem(item);
        return bookingMapper.bookingToBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto changeBookingStatus(Long bookingId, Boolean approved, Long ownerId) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> {
            log.warn("User with id = {} not found", ownerId);
            return new NotFoundException(String.format("User with id = %d not found", ownerId));
        });
        log.debug("User with id = {} found", ownerId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.warn("Booking with id = {} not found", bookingId);
            return new NotFoundException(String.format("Booking with id = %d not found", bookingId));
        });
        log.debug("Booking with id = {} found", bookingId);
        if (!Objects.equals(booking.getItem().getOwner().getId(), owner.getId())) {
            log.warn("User with id = {} has no permission to change status of booking with id = {}",
                    owner.getId(), booking.getId());
            throw new PermissionException(String.format("User with id = %d has no permission to change status of " +
                    "booking with id = %d", owner.getId(), booking.getId()));
        }
        if (approved == Boolean.TRUE && booking.getStatus().equals(BookingStatus.WAITING)) {
            booking.setStatus(BookingStatus.APPROVED);
        } else if (approved == Boolean.FALSE && booking.getStatus().equals(BookingStatus.WAITING)) {
            booking.setStatus(BookingStatus.REJECTED);
        } else {
            throw new ChangeStatusException(String.format("Booking status with id = %s already approved", bookingId));
        }
        return bookingMapper.bookingToBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long bookingId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id = {} not found", userId);
            return new NotFoundException(String.format("User with id = %d not found", userId));
        });
        log.debug("User with id = {} found", userId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> {
            log.warn("Booking with id = {} not found", bookingId);
            return new NotFoundException(String.format("Booking with id = %d not found", bookingId));
        });
        log.debug("Booking with id = {} found", bookingId);
        if (!(Objects.equals(booking.getBooker().getId(), user.getId())
                || Objects.equals(booking.getItem().getOwner().getId(), user.getId()))) {
            log.warn("User with id = {} has no permission to see data about booking with id = {}",
                    user.getId(), booking.getId());
            throw new PermissionException(String.format("User with id = %d has no permission to see data about " +
                    "booking with id = %d", user.getId(), booking.getId()));
        }
        return bookingMapper.bookingToBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getAllUserBookingsByStatus(BookingStatus bookingStatus, Long userId, int from, int size) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id = {} not found", userId);
            return new NotFoundException(String.format("User with id = %d not found", userId));
        });
        log.debug("User with id = {} found", userId);
        ShareItPageRequest pageRequest = new ShareItPageRequest(from, size, Sort.by("start").descending());
        Page<Booking> bookingsPage;
        switch (bookingStatus) {
            case ALL:
                bookingsPage = bookingRepository.findByBookerId(user.getId(), pageRequest);
                break;
            case CURRENT:
                bookingsPage = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        user.getId(), pageRequest);
                break;
            case PAST:
                bookingsPage = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(user.getId(),
                        pageRequest);
                break;
            case FUTURE:
                bookingsPage = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(user.getId(),
                        pageRequest);
                break;
            case WAITING:
            case REJECTED:
                bookingsPage = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(user.getId(), bookingStatus,
                        pageRequest);
                break;
            default:
                log.warn("Received incorrect booking status = {} from user with id = {}", bookingStatus, user.getId());
                throw new IncorrectDataException(String.format("Received incorrect booking status = %s from user " +
                        "with id = %d", bookingStatus, user.getId()));
        }
        return bookingsPage.getContent().stream()
                .map(bookingMapper::bookingToBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getAllOwnerItemsBookingsByStatus(BookingStatus bookingStatus, Long ownerId, int from,
                                                             int size) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> {
            log.warn("User with id = {} not found", ownerId);
            return new NotFoundException(String.format("User with id = %d not found", ownerId));
        });
        log.debug("User with id = {} found", ownerId);
        ShareItPageRequest pageRequest = new ShareItPageRequest(from, size, Sort.by("start").descending());
        Page<Booking> bookingPage;
        switch (bookingStatus) {
            case ALL:
                bookingPage = bookingRepository.findAllByItemOwnerOrderByStartDesc(owner, pageRequest);
                break;
            case CURRENT:
                bookingPage = bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        owner.getId(), pageRequest);
                break;
            case PAST:
                bookingPage = bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(owner.getId(),
                        pageRequest);
                break;
            case FUTURE:
                bookingPage = bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(owner.getId(),
                        pageRequest);
                break;
            case WAITING:
            case REJECTED:
                bookingPage = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(),
                        bookingStatus, pageRequest);
                break;
            default:
                log.warn("Received incorrect booking status = {} from user with id = {}", bookingStatus, owner.getId());
                throw new IncorrectDataException(String.format("Received incorrect booking status = %s from user " +
                        "with id = %d", bookingStatus, owner.getId()));
        }
        return bookingPage.getContent().stream()
                .map(bookingMapper::bookingToBookingDto)
                .collect(Collectors.toList());
    }
}
