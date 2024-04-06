package ru.practicum.shareit.request.util.mapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
public class ItemRequestMapperTest {
    @InjectMocks
    ItemRequestMapper itemRequestMapper;

    static List<ItemRequestInfoDto.ItemDto> responses;
    static ItemRequestInfoDto.ItemDto response1;
    static ItemRequestInfoDto.ItemDto response2;

    @BeforeAll
    static void setUp() {
        response1 = ItemRequestInfoDto.ItemDto.builder().id(1L).name("item1")
                .description("description1").available(Boolean.TRUE).requestId(1L).build();
        response2 = ItemRequestInfoDto.ItemDto.builder().id(2L).name("item2")
                .description("description2").available(Boolean.TRUE).requestId(1L).build();
        responses = List.of(response1, response2);
    }

    @ParameterizedTest
    @MethodSource("provideItemRequestsForItemRequestDtoToItemRequest")
    @DisplayName("itemRequestDtoToItemRequest_whenInvoked_thenItemRequestReturned")
    void itemRequestDtoToItemRequest_whenInvoked_thenItemRequestReturned(ItemRequest itemRequest,
                                                                         ItemRequestDto itemRequestDto) {
        ItemRequest convertedItemRequest = itemRequestMapper.itemRequestDtoToItemRequest(itemRequestDto);

        assertNotNull(convertedItemRequest);
        assertEquals(itemRequest.getDescription(), convertedItemRequest.getDescription());
    }

    @ParameterizedTest
    @MethodSource("provideItemRequestsForItemRequestToItemRequestDto")
    @DisplayName("itemRequestToItemRequestDto_whenInvoked_thenItemRequestDtoReturned")
    void itemRequestToItemRequestDto_whenInvoked_thenItemRequestDtoReturned(ItemRequest itemRequest,
                                                                            ItemRequestDto itemRequestDto) {
        ItemRequestDto convertedItemRequestDto = itemRequestMapper.itemRequestToItemRequestDto(itemRequest);

        assertNotNull(convertedItemRequestDto);
        assertEquals(itemRequestDto.getId(), convertedItemRequestDto.getId());
        assertEquals(itemRequestDto.getDescription(), convertedItemRequestDto.getDescription());
        assertEquals(itemRequestDto.getCreated(), convertedItemRequestDto.getCreated());
    }

    @ParameterizedTest
    @MethodSource("provideItemRequestsForItemRequestToItemRequestInfoDto")
    @DisplayName("itemRequestToItemRequestInfoDto_whenInvoked_thenItemRequestInfoDtoReturned")
    void itemRequestToItemRequestInfoDto_whenInvoked_thenItemRequestInfoDtoReturned(
            ItemRequest itemRequest, ItemRequestInfoDto itemRequestInfoDto) {
        ItemRequestInfoDto convertedItemRequestInfoDto = itemRequestMapper.itemRequestToItemRequestInfoDto(itemRequest);

        assertNotNull(convertedItemRequestInfoDto);
        assertEquals(itemRequestInfoDto.getId(), convertedItemRequestInfoDto.getId());
        assertEquals(itemRequestInfoDto.getDescription(), convertedItemRequestInfoDto.getDescription());
        assertEquals(itemRequestInfoDto.getCreated(), convertedItemRequestInfoDto.getCreated());
    }

    private static Stream<Arguments> provideItemRequestsForItemRequestDtoToItemRequest() {
        return Stream.of(
                arguments(
                        ItemRequest.builder().description("itemRequestDescription").build(),
                        ItemRequestDto.builder().description("itemRequestDescription").build(),
                        "Obvious item request"),
                arguments(
                        ItemRequest.builder().build(),
                        ItemRequestDto.builder().build(),
                        "Empty item request")
        );
    }

    private static Stream<Arguments> provideItemRequestsForItemRequestToItemRequestDto() {
        LocalDateTime created = LocalDateTime.now();
        return Stream.of(
                arguments(
                        ItemRequest.builder().id(1L).description("itemRequestDescription").created(created).build(),
                        ItemRequestDto.builder().id(1L).description("itemRequestDescription").created(created).build(),
                        "Obvious item request"),
                arguments(
                        ItemRequest.builder().description("itemRequestDescription").created(created).build(),
                        ItemRequestDto.builder().description("itemRequestDescription").created(created).build(),
                        "Item request without id"),
                arguments(
                        ItemRequest.builder().id(1L).created(created).build(),
                        ItemRequestDto.builder().id(1L).created(created).build(),
                        "Item request without description"),
                arguments(
                        ItemRequest.builder().id(1L).description("itemRequestDescription").build(),
                        ItemRequestDto.builder().id(1L).description("itemRequestDescription").build(),
                        "Item request without created date"),
                arguments(
                        ItemRequest.builder().build(),
                        ItemRequestDto.builder().build(),
                        "Empty item request")
        );
    }

    private static Stream<Arguments> provideItemRequestsForItemRequestToItemRequestInfoDto() {
        LocalDateTime created = LocalDateTime.now();
        return Stream.of(
                arguments(
                        ItemRequest.builder().id(1L).description("itemRequestDescription").created(created).build(),
                        ItemRequestInfoDto.builder().id(1L).description("itemRequestDescription")
                                .created(created).build(),
                        "Obvious item request"),
                arguments(
                        ItemRequest.builder().description("itemRequestDescription").created(created).build(),
                        ItemRequestInfoDto.builder().description("itemRequestDescription").created(created).build(),
                        "Item request without id"),
                arguments(
                        ItemRequest.builder().id(1L).created(created).build(),
                        ItemRequestInfoDto.builder().id(1L).created(created).build(),
                        "Item request without description"),
                arguments(
                        ItemRequest.builder().id(1L).description("itemRequestDescription").build(),
                        ItemRequestInfoDto.builder().id(1L).description("itemRequestDescription").build(),
                        "Item request without created date"),
                arguments(
                        ItemRequest.builder().build(),
                        ItemRequestInfoDto.builder().build(),
                        "Empty item request")
        );
    }
}
