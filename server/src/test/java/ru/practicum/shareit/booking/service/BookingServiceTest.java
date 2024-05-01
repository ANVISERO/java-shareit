package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.util.mapper.BookingMapper;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    BookingMapper bookingMapper;
    @InjectMocks
    BookingServiceImpl bookingService;
    User user1;
    User user2;
    UserDto userDto1;
    UserDto userDto2;
    Item item1;
    Item item2;
    ItemDto itemDto1;
    ItemDto itemDto2;
    Booking booking1;
    Booking booking2;
    BookingDto bookingDto1;
    BookingDto bookingDto2;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 2, 1, 0, 0);
        user1 = User.builder().id(1L).name("user1").email("user1@post.com").build();
        user2 = User.builder().id(2L).name("user2").email("user2@post.com").build();
        userDto1 = UserDto.builder().id(1L).name("user1").email("user1@post.com").build();
        userDto2 = UserDto.builder().id(2L).name("user2").email("user2@post.com").build();
        item1 = Item.builder().id(1L).name("item1").description("itemDescription1")
                .available(Boolean.TRUE).owner(user1).build();
        item2 = Item.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).owner(user1).build();
        itemDto1 = ItemDto.builder().id(1L).name("item1").description("description1")
                .available(Boolean.TRUE).build();
        itemDto2 = ItemDto.builder().id(2L).name("item2").description("description2")
                .available(Boolean.TRUE).build();
        booking1 = Booking.builder().id(1L).start(start)
                .end(end).booker(user2).item(item1).build();
        booking2 = Booking.builder().id(2L).start(start.plusMonths(2))
                .end(end.plusMonths(2)).booker(user2).item(item1).build();
        bookingDto1 = BookingDto.builder().id(1L).start(start)
                .end(end).booker(userDto2).item(itemDto1).build();
        bookingDto2 = BookingDto.builder().id(2L).start(start.plusMonths(2))
                .end(end.plusMonths(2)).booker(userDto2).item(itemDto1).build();
    }

    @Test
    @DisplayName("createBooking_whenInvoked_thenSavedBookingDtoWithIdReturned")
    void createBooking_whenInvoked_thenSavedBookingDtoWithIdReturned() {
        Booking bookingToCreate = booking1.toBuilder().id(null).build();
        bookingDto1.setStatus(BookingStatus.WAITING);
        BookingDto bookingDtoCheck = bookingDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking1);
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenReturn(bookingDto1);

        BookingDto bookingDto = bookingService.createBooking(bookingToCreate, user2.getId(), item1.getId());

        assertNotNull(bookingDto);
        assertEquals(bookingDtoCheck, bookingDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingMapper, times(1)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking_whenItemNotAvailable_thenAlreadyBookedExceptionThrown")
    void createBooking_whenItemNotAvailable_thenAlreadyBookedExceptionThrown() {
        item1.setAvailable(Boolean.FALSE);
        Booking bookingToCreate = booking1.toBuilder().id(null).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));

        AlreadyBookedException exception = assertThrows(AlreadyBookedException.class, () ->
                bookingService.createBooking(bookingToCreate, user2.getId(), item1.getId()));

        assertEquals("Item with id = 1 can not be booked", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking_whenBookingEndBeforeStart_thenAlreadyBookedExceptionThrown")
    void createBooking_whenBookingEndBeforeStart_thenAlreadyBookedExceptionThrown() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        Booking bookingToCreate = booking1.toBuilder().id(null).start(start)
                .end(start.minusMonths(1)).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));

        AlreadyBookedException exception = assertThrows(AlreadyBookedException.class, () ->
                bookingService.createBooking(bookingToCreate, user2.getId(), item1.getId()));

        assertEquals("Item with id = 1 can not be booked", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking_whenBookingEndEqualsStart_thenAlreadyBookedExceptionThrown")
    void createBooking_whenBookingEndEqualsStart_thenAlreadyBookedExceptionThrown() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        Booking bookingToCreate = booking1.toBuilder().id(null).start(start)
                .end(start).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));

        AlreadyBookedException exception = assertThrows(AlreadyBookedException.class, () ->
                bookingService.createBooking(bookingToCreate, user2.getId(), item1.getId()));

        assertEquals("Item with id = 1 can not be booked", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking_whenInvokedByOwner_thenPermissionExceptionThrown")
    void createBooking_whenInvokedByOwner_thenPermissionExceptionThrown() {
        Booking bookingToCreate = booking1.toBuilder().id(null).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));

        PermissionException exception = assertThrows(PermissionException.class, () ->
                bookingService.createBooking(bookingToCreate, user1.getId(), item1.getId()));

        assertEquals("User with id = 1 can't book his own item", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking_whenItemNotFound_thenNotFoundExceptionThrown")
    void createBooking_whenItemNotFound_thenNotFoundExceptionThrown() {
        Booking bookingToCreate = booking1.toBuilder().id(null).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(bookingToCreate, user1.getId(), 999L));

        assertEquals("Item with id = 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking_whenUserNotFound_thenNotFoundExceptionThrown")
    void createBooking_whenUserNotFound_thenNotFoundExceptionThrown() {
        Booking bookingToCreate = booking1.toBuilder().id(null).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(bookingToCreate, 999L, item1.getId()));

        assertEquals("User with id = 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("changeBookingStatus_whenOwnerApproved_thenBookingDtoWithChangedBookingStatusReturned")
    void changeBookingStatus_whenOwnerApproved_thenBookingDtoWithChangedBookingStatusReturned() {
        booking1.setStatus(BookingStatus.WAITING);
        Booking bookingToSave = booking1.toBuilder().status(BookingStatus.APPROVED).build();
        bookingDto1.setStatus(BookingStatus.APPROVED);
        BookingDto bookingDtoCheck = bookingDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingToSave);
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenReturn(bookingDto1);

        BookingDto bookingDto = bookingService.changeBookingStatus(booking1.getId(), Boolean.TRUE, user1.getId());

        assertNotNull(bookingDto);
        assertEquals(bookingDtoCheck, bookingDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingMapper, times(1)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("changeBookingStatus_whenOwnerRejected_thenBookingDtoWithChangedBookingStatusReturned")
    void changeBookingStatus_whenOwnerRejected_thenBookingDtoWithChangedBookingStatusReturned() {
        booking1.setStatus(BookingStatus.WAITING);
        Booking bookingToSave = booking1.toBuilder().status(BookingStatus.REJECTED).build();
        bookingDto1.setStatus(BookingStatus.REJECTED);
        BookingDto bookingDtoCheck = bookingDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingToSave);
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenReturn(bookingDto1);

        BookingDto bookingDto = bookingService.changeBookingStatus(booking1.getId(), Boolean.FALSE, user1.getId());

        assertNotNull(bookingDto);
        assertEquals(bookingDtoCheck, bookingDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingMapper, times(1)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("changeBookingStatus_whenIncorrectBookingStatusTrue_thenChangeStatusExceptionThrown")
    void changeBookingStatus_whenIncorrectBookingStatusTrue_thenChangeStatusExceptionThrown() {
        booking1.setStatus(BookingStatus.APPROVED);
        bookingDto1.setStatus(BookingStatus.APPROVED);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));

        ChangeStatusException exception = assertThrows(ChangeStatusException.class, () ->
                bookingService.changeBookingStatus(booking1.getId(), Boolean.TRUE, user1.getId()));

        assertEquals("Booking status with id = 1 already approved", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("changeBookingStatus_whenIncorrectBookingStatusFalse_thenChangeStatusExceptionThrown")
    void changeBookingStatus_whenIncorrectBookingStatusFalse_thenChangeStatusExceptionThrown() {
        booking1.setStatus(BookingStatus.APPROVED);
        bookingDto1.setStatus(BookingStatus.APPROVED);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));

        ChangeStatusException exception = assertThrows(ChangeStatusException.class, () ->
                bookingService.changeBookingStatus(booking1.getId(), Boolean.FALSE, user1.getId()));

        assertEquals("Booking status with id = 1 already approved", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("changeBookingStatus_whenNotOwnerInvoked_thenPermissionExceptionThrown")
    void changeBookingStatus_whenNotOwnerInvoked_thenPermissionExceptionThrown() {
        booking1.setStatus(BookingStatus.WAITING);
        bookingDto1.setStatus(BookingStatus.WAITING);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));

        PermissionException exception = assertThrows(PermissionException.class, () ->
                bookingService.changeBookingStatus(booking1.getId(), Boolean.TRUE, user2.getId()));

        assertEquals("User with id = 2 has no permission to change status of booking with id = 1",
                exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("changeBookingStatus_whenBookingNotFound_theNotFoundExceptionThrown")
    void changeBookingStatus_whenBookingNotFound_theNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.changeBookingStatus(999L, Boolean.TRUE, user2.getId()));

        assertEquals("Booking with id = 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("changeBookingStatus_whenUserNotFound_theNotFoundExceptionThrown")
    void changeBookingStatus_whenUserNotFound_theNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.changeBookingStatus(booking1.getId(), Boolean.TRUE, 999L));

        assertEquals("User with id = 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getBookingById_whenInvokedByOwner_thenBookingDtoReturned")
    void getBookingById_whenInvokedByOwner_thenBookingDtoReturned() {
        BookingDto bookingDtoCheck = bookingDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenReturn(bookingDto1);

        BookingDto bookingDto = bookingService.getBookingById(booking1.getId(), user1.getId());

        assertNotNull(bookingDto);
        assertEquals(bookingDtoCheck, bookingDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingMapper, times(1)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getBookingById_whenInvokedByBooker_thenBookingDtoReturned")
    void getBookingById_whenInvokedByBooker_thenBookingDtoReturned() {
        BookingDto bookingDtoCheck = bookingDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenReturn(bookingDto1);

        BookingDto bookingDto = bookingService.getBookingById(booking1.getId(), user2.getId());

        assertNotNull(bookingDto);
        assertEquals(bookingDtoCheck, bookingDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingMapper, times(1)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getBookingById_whenInvokedBySimpleUser_thenPermissionExceptionThrown")
    void getBookingById_whenInvokedBySimpleUser_thenPermissionExceptionThrown() {
        User user3 = User.builder().id(3L).name("user3").email("user3@post.com").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user3));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking1));

        PermissionException exception = assertThrows(PermissionException.class, () ->
                bookingService.getBookingById(booking1.getId(), user3.getId()));

        assertEquals("User with id = 3 has no permission to see data about booking with id = 1",
                exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getBookingById_whenBookingNotFound_thenNotFoundExceptionThrown")
    void getBookingById_whenBookingNotFound_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.getBookingById(999L, user1.getId()));

        assertEquals("Booking with id = 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getBookingById_whenUserNotFound_thenNotFoundExceptionThrown")
    void getBookingById_whenUserNotFound_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.getBookingById(booking1.getId(), 999L));

        assertEquals("User with id = 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findById(anyLong());
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusAll_thenAlLBookingDtosReturned")
    void getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusAll_thenAlLBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.CURRENT);
        booking1.setStart(bookingTime.minusDays(1));
        booking1.setEnd(bookingTime.plusDays(1));
        booking2.setStatus(BookingStatus.PAST);
        booking2.setStart(bookingTime.minusDays(5));
        booking2.setEnd(bookingTime.minusDays(4));
        Booking booking3 = Booking.builder().id(3L).start(bookingTime.plusDays(10)).end(bookingTime.plusDays(12))
                .booker(user2).item(item1).status(BookingStatus.FUTURE).build();
        Booking booking4 = Booking.builder().id(4L).start(bookingTime.plusMonths(1)).end(bookingTime.plusMonths(2))
                .booker(user2).item(item1).status(BookingStatus.WAITING).build();
        Booking booking5 = Booking.builder().id(5L).start(bookingTime).end(bookingTime.plusDays(1))
                .booker(user2).item(item1).status(BookingStatus.REJECTED).build();
        bookingDto1.setStatus(BookingStatus.CURRENT);
        bookingDto1.setStart(bookingTime.minusDays(1));
        bookingDto1.setEnd(bookingTime.plusDays(1));
        bookingDto2.setStatus(BookingStatus.PAST);
        bookingDto2.setStart(bookingTime.minusDays(5));
        bookingDto2.setEnd(bookingTime.minusDays(4));
        BookingDto bookingDto3 = BookingDto.builder().id(3L).start(bookingTime.plusDays(10))
                .end(bookingTime.plusDays(12)).booker(userDto2).item(itemDto1).status(BookingStatus.FUTURE).build();
        BookingDto bookingDto4 = BookingDto.builder().id(4L).start(bookingTime.plusMonths(1))
                .end(bookingTime.plusMonths(2)).booker(userDto2).item(itemDto1).status(BookingStatus.WAITING).build();
        BookingDto bookingDto5 = BookingDto.builder().id(5L).start(bookingTime)
                .end(bookingTime.plusDays(1)).booker(userDto2).item(itemDto1).status(BookingStatus.REJECTED).build();
        List<Booking> bookings = List.of(booking4, booking3, booking5, booking1, booking2);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        BookingDto bookingDtoCheck3 = bookingDto3.toBuilder().build();
        BookingDto bookingDtoCheck4 = bookingDto4.toBuilder().build();
        BookingDto bookingDtoCheck5 = bookingDto5.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByBookerId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            } else if (Objects.equals(booking, booking3)) {
                return bookingDto3;
            } else if (Objects.equals(booking, booking4)) {
                return bookingDto4;
            } else if (Objects.equals(booking, booking5)) {
                return bookingDto5;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.ALL, user2.getId(),
                2, 5);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(5, bookingDtos.size());
        assertEquals(bookingDtoCheck4, bookingDtos.get(0));
        assertEquals(bookingDtoCheck3, bookingDtos.get(1));
        assertEquals(bookingDtoCheck5, bookingDtos.get(2));
        assertEquals(bookingDtoCheck1, bookingDtos.get(3));
        assertEquals(bookingDtoCheck2, bookingDtos.get(4));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, times(5)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusCurrent" +
            "_thenCurrentBookingDtosReturned")
    void getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusCurrent_thenCurrentBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.CURRENT);
        booking1.setStart(bookingTime.minusDays(1));
        booking1.setEnd(bookingTime.plusDays(1));
        booking2.setStatus(BookingStatus.CURRENT);
        booking2.setStart(bookingTime.minusDays(4));
        booking2.setEnd(bookingTime.plusDays(3));
        booking2.setItem(item2);
        bookingDto1.setStatus(BookingStatus.CURRENT);
        bookingDto1.setStart(bookingTime.minusDays(1));
        bookingDto1.setEnd(bookingTime.plusDays(1));
        bookingDto2.setStatus(BookingStatus.CURRENT);
        bookingDto2.setStart(bookingTime.minusDays(4));
        bookingDto2.setEnd(bookingTime.plusDays(3));
        bookingDto2.setItem(itemDto2);
        List<Booking> bookings = List.of(booking1, booking2);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.CURRENT, user2.getId(),
                2, 2);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(2, bookingDtos.size());
        assertEquals(bookingDtoCheck1, bookingDtos.get(0));
        assertEquals(bookingDtoCheck2, bookingDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, times(2)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusPast_thenPastBookingDtosReturned")
    void getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusPast_thenPastBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.PAST);
        booking1.setStart(bookingTime.minusDays(6));
        booking1.setEnd(bookingTime.minusDays(5));
        booking2.setStatus(BookingStatus.PAST);
        booking2.setStart(bookingTime.minusDays(4));
        booking2.setEnd(bookingTime.plusDays(3));
        bookingDto1.setStatus(BookingStatus.PAST);
        bookingDto1.setStart(bookingTime.minusDays(6));
        bookingDto1.setEnd(bookingTime.minusDays(5));
        bookingDto2.setStatus(BookingStatus.PAST);
        bookingDto2.setStart(bookingTime.minusDays(4));
        bookingDto2.setEnd(bookingTime.minusDays(3));
        List<Booking> bookings = List.of(booking2, booking1);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.PAST, user2.getId(),
                2, 2);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(2, bookingDtos.size());
        assertEquals(bookingDtoCheck2, bookingDtos.get(0));
        assertEquals(bookingDtoCheck1, bookingDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, times(2)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusFuture_thenFutureBookingDtosReturned")
    void getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusFuture_thenFutureBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.FUTURE);
        booking1.setStart(bookingTime.plusDays(2));
        booking1.setEnd(bookingTime.plusDays(3));
        booking2.setStatus(BookingStatus.FUTURE);
        booking2.setStart(bookingTime.plusDays(4));
        booking2.setEnd(bookingTime.plusDays(5));
        bookingDto1.setStatus(BookingStatus.FUTURE);
        bookingDto1.setStart(bookingTime.plusDays(2));
        bookingDto1.setEnd(bookingTime.plusDays(3));
        bookingDto2.setStatus(BookingStatus.FUTURE);
        bookingDto2.setStart(bookingTime.plusDays(4));
        bookingDto2.setEnd(bookingTime.plusDays(5));
        List<Booking> bookings = List.of(booking2, booking1);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.FUTURE, user2.getId(),
                2, 2);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(2, bookingDtos.size());
        assertEquals(bookingDtoCheck2, bookingDtos.get(0));
        assertEquals(bookingDtoCheck1, bookingDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, times(2)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusWaiting_" +
            "thenWaitingBookingDtosReturned")
    void getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusWaiting_thenWaitingBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.WAITING);
        booking1.setStart(bookingTime.plusDays(2));
        booking1.setEnd(bookingTime.plusDays(3));
        booking2.setStatus(BookingStatus.WAITING);
        booking2.setStart(bookingTime.plusDays(4));
        booking2.setEnd(bookingTime.plusDays(5));
        bookingDto1.setStatus(BookingStatus.WAITING);
        bookingDto1.setStart(bookingTime.plusDays(2));
        bookingDto1.setEnd(bookingTime.plusDays(3));
        bookingDto2.setStatus(BookingStatus.WAITING);
        bookingDto2.setStart(bookingTime.plusDays(4));
        bookingDto2.setEnd(bookingTime.plusDays(5));
        List<Booking> bookings = List.of(booking2, booking1);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.WAITING, user2.getId(),
                2, 2);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(2, bookingDtos.size());
        assertEquals(bookingDtoCheck2, bookingDtos.get(0));
        assertEquals(bookingDtoCheck1, bookingDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, times(2)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusRejected_" +
            "thenRejectedBookingDtosReturned")
    void getAllUserBookingsByStatus_whenInvokedByUserBookingsWithStatusRejected_thenRejectedBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.REJECTED);
        booking1.setStart(bookingTime.plusDays(2));
        booking1.setEnd(bookingTime.plusDays(3));
        booking2.setStatus(BookingStatus.REJECTED);
        booking2.setStart(bookingTime.plusDays(4));
        booking2.setEnd(bookingTime.plusDays(5));
        bookingDto1.setStatus(BookingStatus.REJECTED);
        bookingDto1.setStart(bookingTime.plusDays(2));
        bookingDto1.setEnd(bookingTime.plusDays(3));
        bookingDto2.setStatus(BookingStatus.REJECTED);
        bookingDto2.setStart(bookingTime.plusDays(4));
        bookingDto2.setEnd(bookingTime.plusDays(5));
        List<Booking> bookings = List.of(booking2, booking1);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.REJECTED, user2.getId(),
                2, 2);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(2, bookingDtos.size());
        assertEquals(bookingDtoCheck2, bookingDtos.get(0));
        assertEquals(bookingDtoCheck1, bookingDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, times(2)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenInvokedByUserWithIncorrectStatus_thenIncorrectDataExceptionThrown")
    void getAllUserBookingsByStatus_whenInvokedByUserWithIncorrectStatus_thenIncorrectDataExceptionThrown() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.CANCELED);
        booking1.setStart(bookingTime.plusDays(2));
        booking1.setEnd(bookingTime.plusDays(3));
        booking2.setStatus(BookingStatus.CANCELED);
        booking2.setStart(bookingTime.plusDays(4));
        booking2.setEnd(bookingTime.plusDays(5));
        bookingDto1.setStatus(BookingStatus.CANCELED);
        bookingDto1.setStart(bookingTime.plusDays(2));
        bookingDto1.setEnd(bookingTime.plusDays(3));
        bookingDto2.setStatus(BookingStatus.CANCELED);
        bookingDto2.setStart(bookingTime.plusDays(4));
        bookingDto2.setEnd(bookingTime.plusDays(5));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));

        IncorrectDataException exception = assertThrows(IncorrectDataException.class, () ->
                bookingService.getAllUserBookingsByStatus(BookingStatus.CANCELED, user2.getId(),
                2, 2));

        assertEquals("Received incorrect booking status = CANCELED from user with id = 2",
                exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenUserNotFound_thenNotFoundExceptionThrown")
    void getAllUserBookingsByStatus_whenUserNotFound_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.getAllUserBookingsByStatus(BookingStatus.ALL, 999L, 2, 5));

        assertEquals("User with id = 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenBookingsWithStatusAllNotFound_thenEmptyListReturned")
    void getAllUserBookingsByStatus_whenBookingsWithStatusAllNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByBookerId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.ALL, user2.getId(),
                2, 5);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenBookingsWithStatusCurrentNotFound_thenEmptyListReturned")
    void getAllUserBookingsByStatus_whenBookingsWithStatusCurrentNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.CURRENT, user2.getId(),
                2, 2);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenBookingsWithStatusPastNotFound_thenEmptyListReturned")
    void getAllUserBookingsByStatus_whenBookingsWithStatusPastNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.PAST, user2.getId(),
                2, 2);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenBookingsWithStatusFutureNotFound_thenEmptyListReturned")
    void getAllUserBookingsByStatus_whenBookingsWithStatusFutureNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.FUTURE, user2.getId(),
                2, 2);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenBookingsWithStatusWaitingNotFound_thenEmptyListReturned")
    void getAllUserBookingsByStatus_whenBookingsWithStatusWaitingNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.WAITING, user2.getId(),
                2, 2);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllUserBookingsByStatus_whenBookingsWithStatusRejectedNotFound_thenEmptyListReturned")
    void getAllUserBookingsByStatus_whenBookingsWithStatusRejectedNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllUserBookingsByStatus(BookingStatus.REJECTED, user2.getId(),
                2, 2);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findByBookerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }









//
//
@Test
@DisplayName("getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusAll_thenAlLBookingDtosReturned")
void getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusAll_thenAlLBookingDtosReturned() {
    LocalDateTime bookingTime = LocalDateTime.now();
    booking1.setStatus(BookingStatus.CURRENT);
    booking1.setStart(bookingTime.minusDays(1));
    booking1.setEnd(bookingTime.plusDays(1));
    booking2.setStatus(BookingStatus.PAST);
    booking2.setStart(bookingTime.minusDays(5));
    booking2.setEnd(bookingTime.minusDays(4));
    Booking booking3 = Booking.builder().id(3L).start(bookingTime.plusDays(10)).end(bookingTime.plusDays(12))
            .booker(user2).item(item1).status(BookingStatus.FUTURE).build();
    Booking booking4 = Booking.builder().id(4L).start(bookingTime.plusMonths(1)).end(bookingTime.plusMonths(2))
            .booker(user2).item(item1).status(BookingStatus.WAITING).build();
    Booking booking5 = Booking.builder().id(5L).start(bookingTime).end(bookingTime.plusDays(1))
            .booker(user2).item(item1).status(BookingStatus.REJECTED).build();
    bookingDto1.setStatus(BookingStatus.CURRENT);
    bookingDto1.setStart(bookingTime.minusDays(1));
    bookingDto1.setEnd(bookingTime.plusDays(1));
    bookingDto2.setStatus(BookingStatus.PAST);
    bookingDto2.setStart(bookingTime.minusDays(5));
    bookingDto2.setEnd(bookingTime.minusDays(4));
    BookingDto bookingDto3 = BookingDto.builder().id(3L).start(bookingTime.plusDays(10))
            .end(bookingTime.plusDays(12)).booker(userDto2).item(itemDto1).status(BookingStatus.FUTURE).build();
    BookingDto bookingDto4 = BookingDto.builder().id(4L).start(bookingTime.plusMonths(1))
            .end(bookingTime.plusMonths(2)).booker(userDto2).item(itemDto1).status(BookingStatus.WAITING).build();
    BookingDto bookingDto5 = BookingDto.builder().id(5L).start(bookingTime)
            .end(bookingTime.plusDays(1)).booker(userDto2).item(itemDto1).status(BookingStatus.REJECTED).build();
    List<Booking> bookings = List.of(booking4, booking3, booking5, booking1, booking2);
    BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
    BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
    BookingDto bookingDtoCheck3 = bookingDto3.toBuilder().build();
    BookingDto bookingDtoCheck4 = bookingDto4.toBuilder().build();
    BookingDto bookingDtoCheck5 = bookingDto5.toBuilder().build();
    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
    when(bookingRepository.findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(bookings));
    when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
        Booking booking = invocationOnMock.getArgument(0);
        if (Objects.equals(booking, booking1)) {
            return bookingDto1;
        } else if (Objects.equals(booking, booking2)) {
            return bookingDto2;
        } else if (Objects.equals(booking, booking3)) {
            return bookingDto3;
        } else if (Objects.equals(booking, booking4)) {
            return bookingDto4;
        } else if (Objects.equals(booking, booking5)) {
            return bookingDto5;
        }
        return null;
    });

    List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.ALL, user2.getId(),
            2, 5);

    assertNotNull(bookingDtos);
    assertFalse(bookingDtos.isEmpty());
    assertEquals(5, bookingDtos.size());
    assertEquals(bookingDtoCheck4, bookingDtos.get(0));
    assertEquals(bookingDtoCheck3, bookingDtos.get(1));
    assertEquals(bookingDtoCheck5, bookingDtos.get(2));
    assertEquals(bookingDtoCheck1, bookingDtos.get(3));
    assertEquals(bookingDtoCheck2, bookingDtos.get(4));
    verify(userRepository, times(1)).findById(anyLong());
    verify(bookingRepository, times(1))
            .findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
    verify(bookingRepository, never())
            .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
    verify(bookingRepository, never())
            .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
    verify(bookingRepository, never())
            .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
    verify(bookingRepository, never())
            .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
    verify(bookingMapper, times(5)).bookingToBookingDto(any(Booking.class));
}

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusCurrent" +
            "_thenCurrentBookingDtosReturned")
    void getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusCurrent_thenCurrentBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.CURRENT);
        booking1.setStart(bookingTime.minusDays(1));
        booking1.setEnd(bookingTime.plusDays(1));
        booking2.setStatus(BookingStatus.CURRENT);
        booking2.setStart(bookingTime.minusDays(4));
        booking2.setEnd(bookingTime.plusDays(3));
        booking2.setItem(item2);
        bookingDto1.setStatus(BookingStatus.CURRENT);
        bookingDto1.setStart(bookingTime.minusDays(1));
        bookingDto1.setEnd(bookingTime.plusDays(1));
        bookingDto2.setStatus(BookingStatus.CURRENT);
        bookingDto2.setStart(bookingTime.minusDays(4));
        bookingDto2.setEnd(bookingTime.plusDays(3));
        bookingDto2.setItem(itemDto2);
        List<Booking> bookings = List.of(booking1, booking2);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.CURRENT,
                user1.getId(), 2, 2);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(2, bookingDtos.size());
        assertEquals(bookingDtoCheck1, bookingDtos.get(0));
        assertEquals(bookingDtoCheck2, bookingDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                        any(Pageable.class));
        verify(bookingMapper, times(2)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusPast" +
            "_thenPastBookingDtosReturned")
    void getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusPast_thenPastBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.PAST);
        booking1.setStart(bookingTime.minusDays(6));
        booking1.setEnd(bookingTime.minusDays(5));
        booking2.setStatus(BookingStatus.PAST);
        booking2.setStart(bookingTime.minusDays(4));
        booking2.setEnd(bookingTime.plusDays(3));
        bookingDto1.setStatus(BookingStatus.PAST);
        bookingDto1.setStart(bookingTime.minusDays(6));
        bookingDto1.setEnd(bookingTime.minusDays(5));
        bookingDto2.setStatus(BookingStatus.PAST);
        bookingDto2.setStart(bookingTime.minusDays(4));
        bookingDto2.setEnd(bookingTime.minusDays(3));
        List<Booking> bookings = List.of(booking2, booking1);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.PAST,
                user1.getId(), 2, 2);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(2, bookingDtos.size());
        assertEquals(bookingDtoCheck2, bookingDtos.get(0));
        assertEquals(bookingDtoCheck1, bookingDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                        any(Pageable.class));
        verify(bookingMapper, times(2)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusFuture" +
            "_thenFutureBookingDtosReturned")
    void getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusFuture_thenFutureBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.FUTURE);
        booking1.setStart(bookingTime.plusDays(2));
        booking1.setEnd(bookingTime.plusDays(3));
        booking2.setStatus(BookingStatus.FUTURE);
        booking2.setStart(bookingTime.plusDays(4));
        booking2.setEnd(bookingTime.plusDays(5));
        bookingDto1.setStatus(BookingStatus.FUTURE);
        bookingDto1.setStart(bookingTime.plusDays(2));
        bookingDto1.setEnd(bookingTime.plusDays(3));
        bookingDto2.setStatus(BookingStatus.FUTURE);
        bookingDto2.setStart(bookingTime.plusDays(4));
        bookingDto2.setEnd(bookingTime.plusDays(5));
        List<Booking> bookings = List.of(booking2, booking1);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.FUTURE,
                user1.getId(), 2, 2);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(2, bookingDtos.size());
        assertEquals(bookingDtoCheck2, bookingDtos.get(0));
        assertEquals(bookingDtoCheck1, bookingDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                        any(Pageable.class));
        verify(bookingMapper, times(2)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusWaiting" +
            "_thenWaitingBookingDtosReturned")
    void getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusWaiting_thenWaitingBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.WAITING);
        booking1.setStart(bookingTime.plusDays(2));
        booking1.setEnd(bookingTime.plusDays(3));
        booking2.setStatus(BookingStatus.WAITING);
        booking2.setStart(bookingTime.plusDays(4));
        booking2.setEnd(bookingTime.plusDays(5));
        bookingDto1.setStatus(BookingStatus.WAITING);
        bookingDto1.setStart(bookingTime.plusDays(2));
        bookingDto1.setEnd(bookingTime.plusDays(3));
        bookingDto2.setStatus(BookingStatus.WAITING);
        bookingDto2.setStart(bookingTime.plusDays(4));
        bookingDto2.setEnd(bookingTime.plusDays(5));
        List<Booking> bookings = List.of(booking2, booking1);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.WAITING,
                user1.getId(), 2, 2);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(2, bookingDtos.size());
        assertEquals(bookingDtoCheck2, bookingDtos.get(0));
        assertEquals(bookingDtoCheck1, bookingDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                        any(Pageable.class));
        verify(bookingMapper, times(2)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusRejected_" +
            "thenRejectedBookingDtosReturned")
    void getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerBookingsWithStatusRejected_thenRejectedBookingDtosReturned() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.REJECTED);
        booking1.setStart(bookingTime.plusDays(2));
        booking1.setEnd(bookingTime.plusDays(3));
        booking2.setStatus(BookingStatus.REJECTED);
        booking2.setStart(bookingTime.plusDays(4));
        booking2.setEnd(bookingTime.plusDays(5));
        bookingDto1.setStatus(BookingStatus.REJECTED);
        bookingDto1.setStart(bookingTime.plusDays(2));
        bookingDto1.setEnd(bookingTime.plusDays(3));
        bookingDto2.setStatus(BookingStatus.REJECTED);
        bookingDto2.setStart(bookingTime.plusDays(4));
        bookingDto2.setEnd(bookingTime.plusDays(5));
        List<Booking> bookings = List.of(booking2, booking1);
        BookingDto bookingDtoCheck1 = bookingDto1.toBuilder().build();
        BookingDto bookingDtoCheck2 = bookingDto2.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(bookings));
        when(bookingMapper.bookingToBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return bookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return bookingDto2;
            }
            return null;
        });

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.REJECTED,
                user1.getId(), 2, 2);

        assertNotNull(bookingDtos);
        assertFalse(bookingDtos.isEmpty());
        assertEquals(2, bookingDtos.size());
        assertEquals(bookingDtoCheck2, bookingDtos.get(0));
        assertEquals(bookingDtoCheck1, bookingDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, times(2)).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerWithIncorrectStatus_thenIncorrectDataExceptionThrown")
    void getAllOwnerItemsBookingsByStatus_whenInvokedByOwnerWithIncorrectStatus_thenIncorrectDataExceptionThrown() {
        LocalDateTime bookingTime = LocalDateTime.now();
        booking1.setStatus(BookingStatus.CANCELED);
        booking1.setStart(bookingTime.plusDays(2));
        booking1.setEnd(bookingTime.plusDays(3));
        booking2.setStatus(BookingStatus.CANCELED);
        booking2.setStart(bookingTime.plusDays(4));
        booking2.setEnd(bookingTime.plusDays(5));
        bookingDto1.setStatus(BookingStatus.CANCELED);
        bookingDto1.setStart(bookingTime.plusDays(2));
        bookingDto1.setEnd(bookingTime.plusDays(3));
        bookingDto2.setStatus(BookingStatus.CANCELED);
        bookingDto2.setStart(bookingTime.plusDays(4));
        bookingDto2.setEnd(bookingTime.plusDays(5));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        IncorrectDataException exception = assertThrows(IncorrectDataException.class, () ->
                bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.CANCELED, user1.getId(),
                        2, 2));

        assertEquals("Received incorrect booking status = CANCELED from user with id = 1",
                exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenUserNotFound_thenNotFoundExceptionThrown")
    void getAllOwnerItemsBookingsByStatus_whenUserNotFound_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.ALL, 999L, 2, 5));

        assertEquals("User with id = 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusAllNotFound_thenEmptyListReturned")
    void getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusAllNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.ALL, user1.getId(),
                2, 5);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusCurrentNotFound_thenEmptyListReturned")
    void getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusCurrentNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.CURRENT,
                user1.getId(), 2, 2);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusPastNotFound_thenEmptyListReturned")
    void getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusPastNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.PAST,
                user1.getId(), 2, 2);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusFutureNotFound_thenEmptyListReturned")
    void getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusFutureNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(),
                any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.FUTURE,
                user1.getId(), 2, 2);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusWaitingNotFound_thenEmptyListReturned")
    void getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusWaitingNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.WAITING,
                user1.getId(), 2, 2);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusRejectedNotFound_thenEmptyListReturned")
    void getAllOwnerItemsBookingsByStatus_whenBookingsWithStatusRejectedNotFound_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class),
                any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<BookingDto> bookingDtos = bookingService.getAllOwnerItemsBookingsByStatus(BookingStatus.REJECTED,
                user1.getId(), 2, 2);

        assertNotNull(bookingDtos);
        assertTrue(bookingDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllByItemOwnerOrderByStartDesc(any(User.class), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, never())
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any(BookingStatus.class), any(Pageable.class));
        verify(bookingMapper, never()).bookingToBookingDto(any(Booking.class));
    }
}
