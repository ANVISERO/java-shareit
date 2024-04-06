package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.util.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemRequestService itemRequestService;
    @MockBean
    private ItemRequestMapper itemRequestMapper;
    private User user1;
    private ItemRequest itemRequest1;
    private ItemRequestDto itemRequestDto1;
    private ItemRequestInfoDto itemRequestInfoDto1;
    private ItemRequestInfoDto itemRequestInfoDto2;
    private ItemRequestInfoDto.ItemDto itemFromItemRequestInfoDto1;
    private ItemRequestInfoDto.ItemDto itemFromItemRequestInfoDto2;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).name("user1").email("user1@post.com").build();
        LocalDateTime created1 = LocalDateTime.now();
        itemRequest1 = ItemRequest.builder().description("itemRequestDescription1")
                .created(created1).requestor(user1).build();
        itemRequestDto1 = ItemRequestDto.builder().description("itemRequestDescription2")
                .created(created1).build();
        itemRequestInfoDto1 = ItemRequestInfoDto.builder().id(1L).description("itemRequestDescription1")
                .created(created1).build();
        itemRequestInfoDto2 = ItemRequestInfoDto.builder().id(2L).description("itemRequestDescription2")
                .created(created1).build();
        itemFromItemRequestInfoDto1 = ItemRequestInfoDto.ItemDto.builder().id(1L).name("item1")
                .description("itemDescription1").available(Boolean.TRUE).requestId(itemRequest1.getId()).build();
        itemFromItemRequestInfoDto2 = ItemRequestInfoDto.ItemDto.builder().id(2L).name("item2")
                .description("itemDescription2").available(Boolean.TRUE).requestId(itemRequest1.getId()).build();
    }

    @Test
    @SneakyThrows
    @DisplayName("createItemRequest_whenItemRequestIsValid_thenSavedItemRequestDtoReturned")
    void createItemRequest_whenItemRequestIsValid_thenSavedItemRequestDtoReturned() {
        ItemRequestDto itemRequestDtoSaved = itemRequestDto1.toBuilder().id(1L).build();
        when(itemRequestMapper.itemRequestDtoToItemRequest(any(ItemRequestDto.class))).thenReturn(itemRequest1);
        when(itemRequestService.createItemRequest(any(ItemRequest.class), anyLong())).thenReturn(itemRequestDtoSaved);

        mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(itemRequestDto1))
                        .header("X-Sharer-User-Id", user1.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDtoSaved.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDtoSaved.getDescription())))
                .andExpect(jsonPath("$.created",
                        is(itemRequestDtoSaved.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));

        verify(itemRequestMapper, times(1))
                .itemRequestDtoToItemRequest(any(ItemRequestDto.class));
        verify(itemRequestService, times(1))
                .createItemRequest(any(ItemRequest.class), anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("getAllItemRequestsByRequestor_whenInvoked_thenListOfItemRequestInfoDtosReturned")
    void getAllItemRequestsByRequestor_whenInvoked_thenListOfItemRequestInfoDtosReturned() {
        itemRequestInfoDto1.setItems(List.of(itemFromItemRequestInfoDto1, itemFromItemRequestInfoDto2));
        itemRequestInfoDto2.setItems(Collections.emptyList());
        List<ItemRequestInfoDto> itemRequestInfoDtos = List.of(itemRequestInfoDto1, itemRequestInfoDto2);
        when(itemRequestService.getAllItemRequestsByRequestor(anyLong())).thenReturn(itemRequestInfoDtos);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", user1.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestInfoDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestInfoDto1.getDescription())))
                .andExpect(jsonPath("$[0].created",
                        is(itemRequestInfoDto1.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[1].id", is(itemRequestInfoDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(itemRequestInfoDto2.getDescription())))
                .andExpect(jsonPath("$[1].created",
                        is(itemRequestInfoDto2.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].items", empty()));


        verify(itemRequestService, times(1))
                .getAllItemRequestsByRequestor(anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("getSeveralItemRequestsPaginated_whenInvoked_thenListOfItemRequestInfoDtosReturned")
    void getSeveralItemRequestsPaginated_whenInvoked_thenListOfItemRequestInfoDtosReturned() {
        itemRequestInfoDto1.setItems(List.of(itemFromItemRequestInfoDto1, itemFromItemRequestInfoDto2));
        itemRequestInfoDto2.setItems(Collections.emptyList());
        List<ItemRequestInfoDto> itemRequestInfoDtos = List.of(itemRequestInfoDto1, itemRequestInfoDto2);
        when(itemRequestService.getSeveralItemRequestsPaginated(anyInt(), anyInt(), anyLong()))
                .thenReturn(itemRequestInfoDtos);

        mockMvc.perform(get("/requests/all")
                        .requestAttr("from", 2)
                        .requestAttr("size", 2)
                        .header("X-Sharer-User-Id", user1.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestInfoDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestInfoDto1.getDescription())))
                .andExpect(jsonPath("$[0].created",
                        is(itemRequestInfoDto1.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].items", hasSize(2)))
                .andExpect(jsonPath("$[1].id", is(itemRequestInfoDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(itemRequestInfoDto2.getDescription())))
                .andExpect(jsonPath("$[1].created",
                        is(itemRequestInfoDto2.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[1].items", empty()));


        verify(itemRequestService, times(1))
                .getSeveralItemRequestsPaginated(anyInt(), anyInt(), anyLong());
    }

    @Test
    @SneakyThrows
    @DisplayName("getItemRequestById_whenInvoked_thenItemRequestInfoDtoReturned")
    void getItemRequestById_whenInvoked_thenItemRequestInfoDtoReturned() {
        itemRequestInfoDto1.setItems(List.of(itemFromItemRequestInfoDto1, itemFromItemRequestInfoDto2));
        when(itemRequestService.getItemRequestById(anyLong(), anyLong()))
                .thenReturn(itemRequestInfoDto1);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", user1.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestInfoDto1.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestInfoDto1.getDescription())))
                .andExpect(jsonPath("$.created",
                        is(itemRequestInfoDto1.getCreated().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.items", hasSize(2)));


        verify(itemRequestService, times(1))
                .getItemRequestById(anyLong(), anyLong());
    }
}
