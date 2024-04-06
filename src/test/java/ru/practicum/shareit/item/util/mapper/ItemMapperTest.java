package ru.practicum.shareit.item.util.mapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
public class ItemMapperTest {
    @InjectMocks
    ItemMapper itemMapper;

    static ItemRequest itemRequest;
    static User user;

    @BeforeAll
    static void setUp() {
        itemRequest = ItemRequest.builder().id(1L).description("itemRequestDescription")
                .created(LocalDateTime.now()).build();
        user = User.builder().id(1L).name("user").email("user@post.com").build();
    }

    @ParameterizedTest
    @MethodSource("provideItemsForItemDtoToItem")
    @DisplayName("itemDtoToItem_whenInvoked_thenItemReturned")
    void itemDtoToItem_whenInvoked_thenItemReturned(Item item, ItemDto itemDto) {
        Item convertedItem = itemMapper.itemDtoToItem(itemDto);

        assertNotNull(convertedItem);
        assertEquals(item.getId(), convertedItem.getId());
        assertEquals(item.getName(), convertedItem.getName());
        assertEquals(item.getDescription(), convertedItem.getDescription());
        assertEquals(item.getAvailable(), convertedItem.getAvailable());
    }

    @ParameterizedTest
    @MethodSource("provideItemsForItemToItemDto")
    @DisplayName("itemToItemDto_whenInvoked_thenItemDtoReturned")
    void itemToItemDto_whenInvoked_thenItemDtoReturned(Item item, ItemDto itemDto) {
        ItemDto convertedItemDto = itemMapper.itemToItemDto(item);

        assertNotNull(convertedItemDto);
        assertEquals(itemDto.getId(), convertedItemDto.getId());
        assertEquals(itemDto.getName(), convertedItemDto.getName());
        assertEquals(itemDto.getDescription(), convertedItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), convertedItemDto.getAvailable());
        assertEquals(itemDto.getRequestId(), convertedItemDto.getRequestId());
    }

    @ParameterizedTest
    @MethodSource("provideItemsForItemToItemInfoDto")
    @DisplayName("itemToItemInfoDto_whenInvoked_thenItemInfoDtoReturned")
    void itemToItemInfoDto_whenInvoked_thenItemInfoDtoReturned(Item item, ItemInfoDto itemDto) {
        ItemInfoDto convertedItemInfoDto = itemMapper.itemToItemInfoDto(item);

        assertNotNull(convertedItemInfoDto);
        assertEquals(itemDto.getId(), convertedItemInfoDto.getId());
        assertEquals(itemDto.getName(), convertedItemInfoDto.getName());
        assertEquals(itemDto.getDescription(), convertedItemInfoDto.getDescription());
        assertEquals(itemDto.getAvailable(), convertedItemInfoDto.getAvailable());
    }

    @ParameterizedTest
    @MethodSource("provideItemsForUpdateItem")
    @DisplayName("updateItem_whenInvoked_thenItemReturned")
    void updateItem_whenInvoked_thenItemReturned(Item existingItem, Item item, Item updatedItem) {
        Item convertedItem = itemMapper.updateItem(existingItem, item);

        assertNotNull(convertedItem);
        assertEquals(updatedItem.getId(), convertedItem.getId());
        assertEquals(updatedItem.getName(), convertedItem.getName());
        assertEquals(updatedItem.getDescription(), convertedItem.getDescription());
        assertEquals(updatedItem.getAvailable(), convertedItem.getAvailable());
        assertEquals(updatedItem.getOwner(), convertedItem.getOwner());
    }

    @ParameterizedTest
    @MethodSource("provideItemsForItemToItemRequestInfoDtoItemDto")
    @DisplayName("itemToItemRequestInfoDtoItemDto_whenInvoked_thenItemRequestInfoDtoItemDtoReturned")
    void itemToItemRequestInfoDtoItemDto_whenInvoked_thenItemRequestInfoDtoItemDtoReturned(
            Item item, ItemRequestInfoDto.ItemDto itemDto) {
        ItemRequestInfoDto.ItemDto convertedItemDto = itemMapper.itemToItemRequestInfoDtoItemDto(item);

        assertNotNull(convertedItemDto);
        assertEquals(itemDto.getId(), convertedItemDto.getId());
        assertEquals(itemDto.getName(), convertedItemDto.getName());
        assertEquals(itemDto.getDescription(), convertedItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), convertedItemDto.getAvailable());
        assertEquals(itemDto.getRequestId(), convertedItemDto.getRequestId());
    }

    private static Stream<Arguments> provideItemsForItemDtoToItem() {
        return Stream.of(
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).build(),
                        ItemDto.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).build(),
                        "Obvious item"),
                arguments(
                        Item.builder().name("item").description("itemDescription").available(Boolean.TRUE).build(),
                        ItemDto.builder().name("item").description("itemDescription").available(Boolean.TRUE).build(),
                        "Item without id"),
                arguments(
                        Item.builder().id(1L).description("itemDescription").available(Boolean.TRUE).build(),
                        ItemDto.builder().id(1L).description("itemDescription").available(Boolean.TRUE).build(),
                        "Item without name"),
                arguments(
                        Item.builder().id(1L).name("item").available(Boolean.TRUE).build(),
                        ItemDto.builder().id(1L).name("item").available(Boolean.TRUE).build(),
                        "Item without description"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription").build(),
                        ItemDto.builder().id(1L).name("item").description("itemDescription").build(),
                        "Item without available status"),
                arguments(
                        Item.builder().build(),
                        ItemDto.builder().build(),
                        "Empty item")
        );
    }

    private static Stream<Arguments> provideItemsForItemToItemDto() {
        return Stream.of(
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).itemRequest(itemRequest).build(),
                        ItemDto.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).requestId(1L).build(),
                        "Obvious item"),
                arguments(
                        Item.builder().name("item").description("itemDescription")
                                .available(Boolean.TRUE).itemRequest(itemRequest).build(),
                        ItemDto.builder().name("item").description("itemDescription")
                                .available(Boolean.TRUE).requestId(1L).build(),
                        "Item without id"),
                arguments(
                        Item.builder().id(1L).description("itemDescription")
                                .available(Boolean.TRUE).itemRequest(itemRequest).build(),
                        ItemDto.builder().id(1L).description("itemDescription")
                                .available(Boolean.TRUE).requestId(1L).build(),
                        "Item without name"),
                arguments(
                        Item.builder().id(1L).name("item").available(Boolean.TRUE)
                                .itemRequest(itemRequest).build(),
                        ItemDto.builder().id(1L).name("item").available(Boolean.TRUE)
                                .requestId(1L).build(),
                        "Item without description"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .itemRequest(itemRequest).build(),
                        ItemDto.builder().id(1L).name("item").description("itemDescription")
                                .requestId(1L).build(),
                        "Item without available status"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription").build(),
                        ItemDto.builder().id(1L).name("item").description("itemDescription").build(),
                        "Item without item request"),
                arguments(
                        Item.builder().build(),
                        ItemDto.builder().build(),
                        "Empty item")
        );
    }

    private static Stream<Arguments> provideItemsForItemToItemInfoDto() {
        return Stream.of(
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).build(),
                        ItemInfoDto.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).build(),
                        "Obvious item"),
                arguments(
                        Item.builder().name("item").description("itemDescription").available(Boolean.TRUE).build(),
                        ItemInfoDto.builder().name("item").description("itemDescription").available(Boolean.TRUE).build(),
                        "Item without id"),
                arguments(
                        Item.builder().id(1L).description("itemDescription").available(Boolean.TRUE).build(),
                        ItemInfoDto.builder().id(1L).description("itemDescription").available(Boolean.TRUE).build(),
                        "Item without name"),
                arguments(
                        Item.builder().id(1L).name("item").available(Boolean.TRUE).build(),
                        ItemInfoDto.builder().id(1L).name("item").available(Boolean.TRUE).build(),
                        "Item without description"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription").build(),
                        ItemInfoDto.builder().id(1L).name("item").description("itemDescription").build(),
                        "Item without available status"),
                arguments(
                        Item.builder().build(),
                        ItemInfoDto.builder().build(),
                        "Empty item")
        );
    }

    private static Stream<Arguments> provideItemsForUpdateItem() {
        return Stream.of(
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        "Obvious item"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        Item.builder().name("updatedItem").build(),
                        Item.builder().id(1L).name("updatedItem").description("itemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        "Update name only"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        Item.builder().description("updatedItemDescription").build(),
                        Item.builder().id(1L).name("item").description("updatedItemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        "Update description only"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        Item.builder().available(Boolean.FALSE).build(),
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.FALSE).owner(user).build(),
                        "Update available status only"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        Item.builder().name("updatedItem").description("updatedItemDescription")
                                .available(Boolean.FALSE).build(),
                        Item.builder().id(1L).name("updatedItem").description("updatedItemDescription")
                                .available(Boolean.FALSE).owner(user).build(),
                        "Update name, description, available status"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        Item.builder().build(),
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).owner(user).build(),
                        "Update nothing")
        );
    }

    private static Stream<Arguments> provideItemsForItemToItemRequestInfoDtoItemDto() {
        return Stream.of(
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).itemRequest(itemRequest).build(),
                        ItemRequestInfoDto.ItemDto.builder().id(1L).name("item").description("itemDescription")
                                .available(Boolean.TRUE).requestId(1L).build(),
                        "Obvious item"),
                arguments(
                        Item.builder().name("item").description("itemDescription")
                                .available(Boolean.TRUE).itemRequest(itemRequest).build(),
                        ItemRequestInfoDto.ItemDto.builder().name("item").description("itemDescription")
                                .available(Boolean.TRUE).requestId(1L).build(),
                        "Item without id"),
                arguments(
                        Item.builder().id(1L).description("itemDescription")
                                .available(Boolean.TRUE).itemRequest(itemRequest).build(),
                        ItemRequestInfoDto.ItemDto.builder().id(1L).description("itemDescription")
                                .available(Boolean.TRUE).requestId(1L).build(),
                        "Item without name"),
                arguments(
                        Item.builder().id(1L).name("item").available(Boolean.TRUE)
                                .itemRequest(itemRequest).build(),
                        ItemRequestInfoDto.ItemDto.builder().id(1L).name("item").available(Boolean.TRUE)
                                .requestId(1L).build(),
                        "Item without description"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription")
                                .itemRequest(itemRequest).build(),
                        ItemRequestInfoDto.ItemDto.builder().id(1L).name("item").description("itemDescription")
                                .requestId(1L).build(),
                        "Item without available status"),
                arguments(
                        Item.builder().id(1L).name("item").description("itemDescription").build(),
                        ItemRequestInfoDto.ItemDto.builder().id(1L).name("item").description("itemDescription").build(),
                        "Item without item request"),
                arguments(
                        Item.builder().build(),
                        ItemRequestInfoDto.ItemDto.builder().build(),
                        "Empty item")
        );
    }
}
