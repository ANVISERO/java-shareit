package ru.practicum.shareit.booking.util.mapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.util.mapper.ItemMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.util.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingMapperTest {
    @Mock
    UserMapper userMapper;
    @Mock
    ItemMapper itemMapper;

    @InjectMocks
    BookingMapper bookingMapper;

    static UserDto userDto;
    static ItemDto itemDto;
    static User user;
    static Item item;

    @BeforeAll
    static void setUp() {
        userDto = UserDto.builder().id(1L).name("user").email("user@post.com").build();
        itemDto = ItemDto.builder().id(1L).name("item").description("description").available(Boolean.TRUE).build();
        user = User.builder().id(1L).name("user").email("user@post.com").build();
        item = Item.builder().id(1L).name("item").description("description").available(Boolean.TRUE).build();
    }

    @ParameterizedTest
    @MethodSource("provideBookingsForBookingDtoToBooking")
    @DisplayName("bookingDtoToBooking_whenInvoked_thenUserReturned")
    void bookingDtoToBooking_whenInvoked_thenBookingReturned(Booking booking, BookingDto bookingDto) {
        Booking convertedBooking = bookingMapper.bookingDtoToBooking(bookingDto);

        assertNotNull(convertedBooking);
        assertEquals(booking.getId(), convertedBooking.getId());
        assertEquals(booking.getStatus(), convertedBooking.getStatus());
        assertEquals(booking.getStart(), convertedBooking.getStart());
        assertEquals(booking.getEnd(), convertedBooking.getEnd());
    }

    @ParameterizedTest
    @MethodSource("provideBookingsForBookingToBookingDto")
    @DisplayName("bookingToBookingDto_whenInvoked_thenBookingDtoReturned")
    void bookingToBookingDto_whenInvoked_thenBookingDtoReturned(Booking booking, BookingDto bookingDto) {
        when(userMapper.userToUserDto(any(User.class))).thenReturn(userDto);
        when(itemMapper.itemToItemDto(any(Item.class))).thenReturn(itemDto);

        BookingDto convertedBooking = bookingMapper.bookingToBookingDto(booking);

        assertNotNull(convertedBooking);
        assertEquals(bookingDto.getId(), convertedBooking.getId());
        assertEquals(bookingDto.getStatus(), convertedBooking.getStatus());
        assertEquals(bookingDto.getStart(), convertedBooking.getStart());
        assertEquals(bookingDto.getEnd(), convertedBooking.getEnd());
        assertEquals(userDto, convertedBooking.getBooker());
        assertEquals(itemDto, convertedBooking.getItem());
    }

    @ParameterizedTest
    @MethodSource("provideBookingsForBookingToBookingDtoWithoutUserAndItem")
    @DisplayName("bookingToItemInfoDtoBookingDto_whenInvoked_thenItemInfoDtoBookingDtoReturned")
    void bookingToItemInfoDtoBookingDto_whenInvoked_thenItemInfoDtoBookingDtoReturned(
            Booking booking, ItemInfoDto.BookingDto bookingDto) {
        ItemInfoDto.BookingDto convertedBooking = bookingMapper.bookingToItemInfoDtoBookingDto(booking);

        assertNotNull(convertedBooking);
        assertEquals(bookingDto.getId(), convertedBooking.getId());
        assertEquals(bookingDto.getStatus(), convertedBooking.getStatus());
        assertEquals(bookingDto.getStart(), convertedBooking.getStart());
        assertEquals(bookingDto.getEnd(), convertedBooking.getEnd());
        assertEquals(bookingDto.getItemId(), convertedBooking.getItemId());
        assertEquals(bookingDto.getBookerId(), convertedBooking.getBookerId());
    }

    private static Stream<Arguments> provideBookingsForBookingDtoToBooking() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();
        return Stream.of(
                arguments(
                        Booking.builder().id(1L).status(BookingStatus.APPROVED).start(start).end(end).build(),
                        BookingDto.builder().id(1L).status(BookingStatus.APPROVED).start(start).end(end).build(),
                        "Obvious booking"),
                arguments(
                        Booking.builder().status(BookingStatus.APPROVED).start(start).end(end).build(),
                        BookingDto.builder().status(BookingStatus.APPROVED).start(start).end(end).build(),
                        "Booking without id"),
                arguments(
                        Booking.builder().id(1L).start(start).end(end).build(),
                        BookingDto.builder().id(1L).start(start).end(end).build(),
                        "Booking without status"),
                arguments(
                        Booking.builder().id(1L).status(BookingStatus.APPROVED).end(end).build(),
                        BookingDto.builder().id(1L).status(BookingStatus.APPROVED).end(end).build(),
                        "Booking without start"),
                arguments(
                        Booking.builder().id(1L).status(BookingStatus.APPROVED).start(start).build(),
                        BookingDto.builder().id(1L).status(BookingStatus.APPROVED).start(start).build(),
                        "Booking without end"),
                arguments(
                        Booking.builder().build(),
                        BookingDto.builder().build(),
                        "Empty booking")
        );
    }

    private static Stream<Arguments> provideBookingsForBookingToBookingDto() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();
        return Stream.of(
                arguments(
                        Booking.builder().id(1L).status(BookingStatus.APPROVED)
                                .start(start).end(end).booker(user).item(item).build(),
                        BookingDto.builder().id(1L).status(BookingStatus.APPROVED)
                                .start(start).end(end).booker(userDto).item(itemDto).build(),
                        "Obvious booking"),
                arguments(
                        Booking.builder().status(BookingStatus.APPROVED)
                                .start(start).end(end).booker(user).item(item).build(),
                        BookingDto.builder().status(BookingStatus.APPROVED)
                                .start(start).end(end).booker(userDto).item(itemDto).build(),
                        "Booking without id"),
                arguments(
                        Booking.builder().id(1L).start(start).end(end).booker(user).item(item).build(),
                        BookingDto.builder().id(1L).start(start).end(end).booker(userDto).item(itemDto).build(),
                        "Booking without status"),
                arguments(
                        Booking.builder().id(1L).status(BookingStatus.APPROVED)
                                .end(end).booker(user).item(item).build(),
                        BookingDto.builder().id(1L).status(BookingStatus.APPROVED)
                                .end(end).booker(userDto).item(itemDto).build(),
                        "Booking without start"),
                arguments(
                        Booking.builder().id(1L).status(BookingStatus.APPROVED)
                                .start(start).booker(user).item(item).build(),
                        BookingDto.builder().id(1L).status(BookingStatus.APPROVED)
                                .start(start).booker(userDto).item(itemDto).build(),
                        "Booking without end"),
                arguments(
                        Booking.builder().booker(user).item(item).build(),
                        BookingDto.builder().booker(userDto).item(itemDto).build(),
                        "Empty booking")
        );
    }

    private static Stream<Arguments> provideBookingsForBookingToBookingDtoWithoutUserAndItem() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();
        return Stream.of(
                arguments(
                        Booking.builder().id(1L).status(BookingStatus.APPROVED)
                                .start(start).end(end).booker(user).item(item).build(),
                        ItemInfoDto.BookingDto.builder().id(1L).status(BookingStatus.APPROVED)
                                .start(start).end(end).itemId(item.getId()).bookerId(user.getId()).build(),
                        "Obvious booking"),
                arguments(
                        Booking.builder().status(BookingStatus.APPROVED)
                                .start(start).end(end).booker(user).item(item).build(),
                        ItemInfoDto.BookingDto.builder().status(BookingStatus.APPROVED)
                                .start(start).end(end).itemId(item.getId()).bookerId(user.getId()).build(),
                        "Booking without id"),
                arguments(
                        Booking.builder().id(1L).start(start).end(end).booker(user).item(item).build(),
                        ItemInfoDto.BookingDto.builder().id(1L).start(start).end(end)
                                .itemId(item.getId()).bookerId(user.getId()).build(),
                        "Booking without status"),
                arguments(
                        Booking.builder().id(1L).status(BookingStatus.APPROVED)
                                .end(end).booker(user).item(item).build(),
                        ItemInfoDto.BookingDto.builder().id(1L).status(BookingStatus.APPROVED)
                                .end(end).itemId(item.getId()).bookerId(user.getId()).build(),
                        "Booking without start"),
                arguments(
                        Booking.builder().id(1L).status(BookingStatus.APPROVED)
                                .start(start).booker(user).item(item).build(),
                        ItemInfoDto.BookingDto.builder().id(1L).status(BookingStatus.APPROVED)
                                .start(start).itemId(item.getId()).bookerId(user.getId()).build(),
                        "Booking without end"),
                arguments(
                        Booking.builder().booker(user).item(item).build(),
                        ItemInfoDto.BookingDto.builder().itemId(item.getId()).bookerId(user.getId()).build(),
                        "Empty booking")
        );
    }
}
