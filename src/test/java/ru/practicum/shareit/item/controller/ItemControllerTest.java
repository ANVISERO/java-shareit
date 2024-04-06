package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.util.mapper.CommentMapper;
import ru.practicum.shareit.item.util.mapper.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;
    @MockBean
    private ItemMapper itemMapper;
    @MockBean
    private CommentMapper commentMapper;
    User user1;
    User user2;
    ItemRequest itemRequest1;
    Item item1;
    ItemDto itemDto1;
    Booking booking1;
    Booking booking2;
    Booking bookingNotLast1;
    Booking bookingNotNext1;
    ItemInfoDto.BookingDto itemInfoDtoBookingDto1;
    ItemInfoDto.BookingDto itemInfoDtoBookingDto2;
    ItemInfoDto.BookingDto itemInfoDtoBookingDtoNotLast1;
    ItemInfoDto.BookingDto itemInfoDtoBookingDtoNotNext1;
    Comment comment1;
    Comment comment2;
    CommentDto commentDto1;
    CommentDto commentDto2;
    ItemInfoDto itemInfoDto1;

    @BeforeEach
    void setUp() {
        LocalDateTime created1 = LocalDateTime.now();
        user1 = User.builder().id(1L).name("user1").email("user1@post.com").build();
        user2 = User.builder().id(2L).name("user2").email("user2@post.com").build();
        itemRequest1 = ItemRequest.builder().id(1L).description("itemRequestDescription1")
                .created(created1).requestor(user1).build();
        item1 = Item.builder().name("item1").description("itemDescription1")
                .available(Boolean.TRUE).owner(user1).itemRequest(itemRequest1).build();
        itemDto1 = ItemDto.builder().name("item1").description("description1")
                .available(Boolean.TRUE).requestId(itemRequest1.getId()).build();
        LocalDateTime start1 = LocalDateTime.now();
        booking1 = Booking.builder().id(1L).status(BookingStatus.PAST).start(start1.minusDays(2))
                .end(start1.minusDays(1)).booker(user2).item(item1).build();
        booking2 = Booking.builder().id(2L).status(BookingStatus.FUTURE).start(start1.plusDays(1))
                .end(start1.plusDays(2)).booker(user2).item(item1).build();
        bookingNotLast1 = Booking.builder().id(11L).status(BookingStatus.PAST).start(start1.minusDays(5))
                .end(start1.minusDays(4)).booker(user2).item(item1).build();
        bookingNotNext1 = Booking.builder().id(12L).status(BookingStatus.FUTURE).start(start1.plusDays(4))
                .end(start1.plusDays(5)).booker(user2).item(item1).build();
        itemInfoDtoBookingDto1 = ItemInfoDto.BookingDto.builder().id(1L).status(BookingStatus.PAST)
                .start(start1.minusDays(2)).end(start1.minusDays(1)).bookerId(user2.getId()).itemId(item1.getId())
                .build();
        itemInfoDtoBookingDto2 = ItemInfoDto.BookingDto.builder().id(2L).status(BookingStatus.PAST)
                .start(start1.plusDays(1)).end(start1.plusDays(2)).bookerId(user2.getId()).itemId(item1.getId())
                .build();
        itemInfoDtoBookingDtoNotLast1 = ItemInfoDto.BookingDto.builder().id(11L).status(BookingStatus.PAST)
                .start(start1.minusDays(5)).end(start1.minusDays(4)).bookerId(user2.getId()).itemId(item1.getId())
                .build();
        itemInfoDtoBookingDtoNotNext1 = ItemInfoDto.BookingDto.builder().id(12L).status(BookingStatus.PAST)
                .start(start1.plusDays(4)).end(start1.plusDays(5)).bookerId(user2.getId()).itemId(item1.getId())
                .build();
        comment1 = Comment.builder().id(1L).text("comment1").item(item1).author(user2).created(start1.plusDays(5))
                .build();
        comment2 = Comment.builder().id(2L).text("comment2").item(item1).author(user2).created(start1.plusDays(7))
                .build();
        commentDto1 = CommentDto.builder().text("comment1").created(start1.plusDays(5))
                .authorName(user2.getName()).build();
        commentDto2 = CommentDto.builder().id(2L).text("comment2").created(start1.plusDays(7))
                .authorName(user2.getName()).build();
        itemInfoDto1 = ItemInfoDto.builder().id(1L).name("item1").description("itemDescription1")
                .available(Boolean.TRUE).build();
    }

    @Test
    @SneakyThrows
    @DisplayName("createItem_whenItemIsValid_thenSavedItemDtoReturned")
    void createItem_whenItemIsValid_thenSavedItemDtoReturned() {
        ItemDto itemDtoSaved = itemDto1.toBuilder().id(1L).build();
        when(itemMapper.itemDtoToItem(any(ItemDto.class))).thenReturn(item1);
        when(itemService.createItem(any(Item.class), anyLong(), anyLong())).thenReturn(itemDtoSaved);

        mockMvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDto1))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoSaved.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoSaved.getName())))
                .andExpect(jsonPath("$.description", is(itemDtoSaved.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoSaved.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDtoSaved.getRequestId()), Long.class));

        verify(itemMapper, times(1)).itemDtoToItem(any(ItemDto.class));
        verify(itemService, times(1)).createItem(any(Item.class), anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("getAllUserItems_whenInvoked_thenListOfItemInfoDtosReturned")
    void getAllUserItems_whenInvoked_thenListOfItemInfoDtosReturned() {
        itemInfoDto1.setLastBooking(itemInfoDtoBookingDto1);
        itemInfoDto1.setNextBooking(itemInfoDtoBookingDto2);
        itemInfoDto1.setComments(List.of(commentDto1, commentDto2));
        ItemInfoDto itemInfoDto2 = ItemInfoDto.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).build();
        itemInfoDto2.setComments(Collections.emptyList());
        List<ItemInfoDto> itemInfoDtos = List.of(itemInfoDto1, itemInfoDto2);
        when(itemService.getAllUserItems(anyLong(), anyInt(), anyInt())).thenReturn(itemInfoDtos);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .requestAttr("from", 2)
                        .requestAttr("size", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemInfoDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemInfoDto1.getName())))
                .andExpect(jsonPath("$[0].description", is(itemInfoDto1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemInfoDto1.getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking.id", is(itemInfoDtoBookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].nextBooking.id", is(itemInfoDtoBookingDto2.getId()), Long.class))
                .andExpect(jsonPath("$[0].comments", hasSize(2)))
                .andExpect(jsonPath("$[1].id", is(itemInfoDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemInfoDto2.getName())))
                .andExpect(jsonPath("$[1].description", is(itemInfoDto2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemInfoDto2.getAvailable())))
                .andExpect(jsonPath("$[1].lastBooking.id").doesNotExist())
                .andExpect(jsonPath("$[1].nextBooking.id").doesNotExist())
                .andExpect(jsonPath("$[1].comments", empty()));

        verify(itemService, times(1)).getAllUserItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    @DisplayName("getItemById_whenInvoked_thenItemInfoDtoReturned")
    void getItemById_whenInvoked_thenItemInfoDtoReturned() {
        itemInfoDto1.setLastBooking(itemInfoDtoBookingDto1);
        itemInfoDto1.setNextBooking(itemInfoDtoBookingDto2);
        itemInfoDto1.setComments(List.of(commentDto1, commentDto2));
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemInfoDto1);

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemInfoDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemInfoDto1.getName())))
                .andExpect(jsonPath("$.description", is(itemInfoDto1.getDescription())))
                .andExpect(jsonPath("$.available", is(itemInfoDto1.getAvailable())))
                .andExpect(jsonPath("$.lastBooking.id", is(itemInfoDtoBookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$.nextBooking.id", is(itemInfoDtoBookingDto2.getId()), Long.class))
                .andExpect(jsonPath("$.comments", hasSize(2)));

        verify(itemService, times(1)).getItemById(anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("updateItem_whenItemUpdatesAreValid_thenUpdatedItemDtoReturned")
    void updateItem_whenItemUpdatesAreValid_thenUpdatedItemDtoReturned() {
        ItemDto itemDtoUpdates = ItemDto.builder().name("updatedName").description("updatedDescription").build();
        itemDto1.setName(itemDtoUpdates.getName());
        itemDto1.setDescription(itemDtoUpdates.getDescription());
        item1.setName(itemDtoUpdates.getName());
        item1.setDescription(itemDtoUpdates.getDescription());
        when(itemMapper.itemDtoToItem(any(ItemDto.class))).thenReturn(item1);
        when(itemService.updateItem(anyLong(), any(Item.class), anyLong())).thenReturn(itemDto1);

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .content(objectMapper.writeValueAsString(itemDtoUpdates))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto1.getName())))
                .andExpect(jsonPath("$.description", is(itemDto1.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto1.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto1.getRequestId()), Long.class));

        verify(itemMapper, times(1)).itemDtoToItem(any(ItemDto.class));
        verify(itemService, times(1)).updateItem(anyLong(), any(Item.class), anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("searchItems_whenInvoked_thenListOfItemDtosReturned")
    void searchItems_whenInvoked_thenListOfItemDtosReturned() {
        ItemDto itemDto2 = ItemDto.builder().name("item2").description("description2")
                .available(Boolean.TRUE).requestId(itemRequest1.getId()).build();
        List<ItemDto> itemInfoDtos = List.of(itemDto1, itemDto2);
        when(itemService.searchItems(anyString(), anyInt(), anyInt())).thenReturn(itemInfoDtos);

        mockMvc.perform(get("/items/search")
                        .param("text", "item")
                        .requestAttr("from", 2)
                        .requestAttr("size", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto1.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto1.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(itemDto1.getRequestId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(itemDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDto2.getName())))
                .andExpect(jsonPath("$[1].description", is(itemDto2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemDto2.getAvailable())))
                .andExpect(jsonPath("$[1].requestId", is(itemDto2.getRequestId()), Long.class));

        verify(itemService, times(1)).searchItems(anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    @DisplayName("createComment_whenCommentIsValid_thenSavedCommentDtoReturned")
    void createComment_whenCommentIsValid_thenSavedCommentDtoReturned() {
        CommentDto commentDtoSaved = commentDto1.toBuilder().id(1L).build();
        when(commentMapper.commentDtoToComment(any(CommentDto.class))).thenReturn(comment1);
        when(itemService.createComment(any(Comment.class), anyLong(), anyLong())).thenReturn(commentDtoSaved);

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .content(objectMapper.writeValueAsString(commentDto1))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDtoSaved.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDtoSaved.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDtoSaved.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentDtoSaved.getCreated()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(commentMapper, times(1)).commentDtoToComment(any(CommentDto.class));
        verify(itemService, times(1)).createComment(any(Comment.class), anyLong(), anyLong());
    }
}
