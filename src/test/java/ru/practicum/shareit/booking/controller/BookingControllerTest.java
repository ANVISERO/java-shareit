package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.util.mapper.BookingMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private BookingMapper bookingMapper;
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
    private final String userHeader = "X-Sharer-User-Id";

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
        booking1 = Booking.builder().start(start)
                .end(end).booker(user2).item(item1).build();
        booking2 = Booking.builder().start(start.plusMonths(2))
                .end(end.plusMonths(2)).booker(user2).item(item1).build();
        bookingDto1 = BookingDto.builder().status(BookingStatus.APPROVED).start(start).end(end)
                .itemId(itemDto1.getId()).bookerId(userDto2.getId()).booker(userDto2).item(itemDto1).build();
        bookingDto2 = BookingDto.builder().status(BookingStatus.APPROVED).start(start.plusMonths(2))
                .end(end.plusMonths(2)).itemId(itemDto1.getId()).bookerId(userDto2.getId())
                .booker(userDto2).item(itemDto1).build();
    }

    @Test
    @SneakyThrows
    @DisplayName("createBooking_whenBookingIsValid_thenSavedBookingDtoReturned")
    void createBooking_whenBookingIsValid_thenSavedBookingDtoReturned() {
        BookingDto bookingDtoSaved = bookingDto1.toBuilder().id(1L).build();
        when(bookingMapper.bookingDtoToBooking(any(BookingDto.class))).thenReturn(booking1);
        when(bookingService.createBooking(any(Booking.class), anyLong(), anyLong())).thenReturn(bookingDtoSaved);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto1))
                        .header(userHeader, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDtoSaved.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDtoSaved.getStatus().toString())))
                .andExpect(jsonPath("$.start", is(bookingDtoSaved.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(bookingDtoSaved.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.itemId", is(bookingDtoSaved.getItemId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(bookingDtoSaved.getBookerId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingDtoSaved.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingDtoSaved.getItem().getId()), Long.class));

        verify(bookingMapper, times(1)).bookingDtoToBooking(any(BookingDto.class));
        verify(bookingService, times(1))
                .createBooking(any(Booking.class), anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("changeBookingStatus_whenInvoked_thenBookingDtoWithChangedStatusReturned")
    void changeBookingStatus_whenInvoked_thenBookingDtoWithChangedStatusReturned() {
        bookingDto1.setId(1L);
        bookingDto1.setStatus(BookingStatus.APPROVED);
        when(bookingService.changeBookingStatus(anyLong(), anyBoolean(), anyLong())).thenReturn(bookingDto1);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .param("approved", Boolean.TRUE.toString())
                        .header(userHeader, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto1.getStatus().toString())))
                .andExpect(jsonPath("$.start", is(bookingDto1.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(bookingDto1.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.itemId", is(bookingDto1.getItemId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(bookingDto1.getBookerId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingDto1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingDto1.getItem().getId()), Long.class));

        verify(bookingService, times(1))
                .changeBookingStatus(anyLong(), anyBoolean(), anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("getBookingById_whenInvoked_thenBookingDtoReturned")
    void getBookingById_whenInvoked_thenBookingDtoReturned() {
        bookingDto1.setId(1L);
        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(bookingDto1);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(userHeader, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto1.getStatus().toString())))
                .andExpect(jsonPath("$.start", is(bookingDto1.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(bookingDto1.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.itemId", is(bookingDto1.getItemId()), Long.class))
                .andExpect(jsonPath("$.bookerId", is(bookingDto1.getBookerId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingDto1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingDto1.getItem().getId()), Long.class));

        verify(bookingService, times(1))
                .getBookingById(anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("getAllUserBookingsByStatus_whenInvoked_thenListOfBookingDtosReturned")
    void getAllUserBookingsByStatus_whenInvoked_thenListOfBookingDtosReturned() {
        bookingDto1.setId(1L);
        bookingDto2.setId(2L);
        List<BookingDto> bookingDtos = List.of(bookingDto1, bookingDto2);
        when(bookingService.getAllUserBookingsByStatus(any(BookingStatus.class), anyLong(), anyInt(), anyInt()))
                .thenReturn(bookingDtos);

        mockMvc.perform(get("/bookings")
                        .param("state", BookingStatus.APPROVED.toString())
                        .requestAttr("from", 2)
                        .requestAttr("size", 2)
                        .header(userHeader, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingDto1.getStatus().toString())))
                .andExpect(jsonPath("$[0].start", is(bookingDto1.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].end", is(bookingDto1.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].itemId", is(bookingDto1.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].bookerId", is(bookingDto1.getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDto1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingDto1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(bookingDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].status", is(bookingDto2.getStatus().toString())))
                .andExpect(jsonPath("$[1].start", is(bookingDto2.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].end", is(bookingDto2.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].itemId", is(bookingDto2.getItemId()), Long.class))
                .andExpect(jsonPath("$[1].bookerId", is(bookingDto2.getBookerId()), Long.class))
                .andExpect(jsonPath("$[1].booker.id", is(bookingDto2.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(bookingDto2.getItem().getId()), Long.class));

        verify(bookingService, times(1))
                .getAllUserBookingsByStatus(any(BookingStatus.class), anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenInvoked_thenListOfBookingDtosReturned")
    void getAllOwnerItemsBookingsByStatus_whenInvoked_thenListOfBookingDtosReturned() {
        bookingDto1.setId(1L);
        bookingDto2.setId(2L);
        List<BookingDto> bookingDtos = List.of(bookingDto1, bookingDto2);
        when(bookingService.getAllOwnerItemsBookingsByStatus(any(BookingStatus.class), anyLong(), anyInt(), anyInt()))
                .thenReturn(bookingDtos);

        mockMvc.perform(get("/bookings/owner")
                        .param("state", BookingStatus.APPROVED.toString())
                        .requestAttr("from", 2)
                        .requestAttr("size", 2)
                        .header(userHeader, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingDto1.getStatus().toString())))
                .andExpect(jsonPath("$[0].start", is(bookingDto1.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].end", is(bookingDto1.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].itemId", is(bookingDto1.getItemId()), Long.class))
                .andExpect(jsonPath("$[0].bookerId", is(bookingDto1.getBookerId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingDto1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingDto1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(bookingDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].status", is(bookingDto2.getStatus().toString())))
                .andExpect(jsonPath("$[1].start", is(bookingDto2.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].end", is(bookingDto2.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].itemId", is(bookingDto2.getItemId()), Long.class))
                .andExpect(jsonPath("$[1].bookerId", is(bookingDto2.getBookerId()), Long.class))
                .andExpect(jsonPath("$[1].booker.id", is(bookingDto2.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(bookingDto2.getItem().getId()), Long.class));

        verify(bookingService, times(1))
                .getAllOwnerItemsBookingsByStatus(any(BookingStatus.class), anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    @DisplayName("getAllOwnerItemsBookingsByStatus_whenIncorrectBookingStatus_thenBadRequestReturned")
    void getAllOwnerItemsBookingsByStatus_whenIncorrectBookingStatus_thenBadRequestReturned() {
        mockMvc.perform(get("/bookings/owner")
                        .param("state", "XOXO")
                        .requestAttr("from", 2)
                        .requestAttr("size", 2)
                        .header(userHeader, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingService, never())
                .getAllOwnerItemsBookingsByStatus(any(BookingStatus.class), anyLong(), anyInt(), anyInt());
    }
}
