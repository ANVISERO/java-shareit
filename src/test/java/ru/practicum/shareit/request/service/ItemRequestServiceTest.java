package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.util.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.util.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    ItemMapper itemMapper;
    @Mock
    ItemRequestMapper itemRequestMapper;
    @InjectMocks
    ItemRequestServiceImpl itemRequestService;

    User user1;
    User user2;
    Item item1;
    Item item2;
    Item item3;
    Item item4;
    ItemRequestInfoDto.ItemDto itemFromItemRequestInfoDto1;
    ItemRequestInfoDto.ItemDto itemFromItemRequestInfoDto2;
    ItemRequestInfoDto.ItemDto itemFromItemRequestInfoDto3;
    ItemRequestInfoDto.ItemDto itemFromItemRequestInfoDto4;
    ItemRequest itemRequest1;
    ItemRequest itemRequest2;
    ItemRequestDto itemRequestDto1;
    ItemRequestDto itemRequestDto2;
    ItemRequestInfoDto itemRequestInfoDto1;
    ItemRequestInfoDto itemRequestInfoDto2;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).name("user1").email("user1@post.com").build();
        user2 = User.builder().id(2L).name("user2").email("user2@post.com").build();
        LocalDateTime created1 = LocalDateTime.now();
        LocalDateTime created2 = LocalDateTime.now();
        itemRequest1 = ItemRequest.builder().id(1L).description("itemRequestDescription1")
                .created(created1).requestor(user1).build();
        itemRequest2 = ItemRequest.builder().id(2L).description("itemRequestDescription2")
                .created(created2).requestor(user1).build();
        itemRequestDto1 = ItemRequestDto.builder().id(1L).description("itemRequestDescription2")
                .created(created1).build();
        itemRequestDto2 = ItemRequestDto.builder().id(2L).description("itemRequestDescription2")
                .created(created2).build();
        itemRequestInfoDto1 = ItemRequestInfoDto.builder().id(1L).description("itemRequestDescription1")
                .created(created1).build();
        itemRequestInfoDto2 = ItemRequestInfoDto.builder().id(2L).description("itemRequestDescription2")
                .created(created1).build();
        item1 = Item.builder().id(1L).name("item1").description("itemDescription1")
                .available(Boolean.TRUE).owner(user2).itemRequest(itemRequest1).build();
        item2 = Item.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).owner(user2).itemRequest(itemRequest1).build();
        item3 = Item.builder().id(3L).name("item3").description("itemDescription3")
                .available(Boolean.TRUE).owner(user2).itemRequest(itemRequest2).build();
        item4 = Item.builder().id(4L).name("item4").description("itemDescription4")
                .available(Boolean.TRUE).owner(user2).itemRequest(itemRequest2).build();
        itemFromItemRequestInfoDto1 = ItemRequestInfoDto.ItemDto.builder().id(1L).name("item1")
                .description("itemDescription1").available(Boolean.TRUE).requestId(itemRequest1.getId()).build();
        itemFromItemRequestInfoDto2 = ItemRequestInfoDto.ItemDto.builder().id(2L).name("item2")
                .description("itemDescription2").available(Boolean.TRUE).requestId(itemRequest1.getId()).build();
        itemFromItemRequestInfoDto3 = ItemRequestInfoDto.ItemDto.builder().id(3L).name("item3")
                .description("itemDescription3").available(Boolean.TRUE).requestId(itemRequest2.getId()).build();
        itemFromItemRequestInfoDto4 = ItemRequestInfoDto.ItemDto.builder().id(4L).name("item4")
                .description("itemDescription4").available(Boolean.TRUE).requestId(itemRequest2.getId()).build();
    }

    @Test
    @DisplayName("createItemRequest_whenInvoked_thenSavedItemRequestWithIdReturned")
    public void createItemRequest_whenInvoked_thenSavedItemRequestWithIdReturned() {
        ItemRequest itemRequest = ItemRequest.builder().description("itemRequestDescription1").build();
        ItemRequestDto itemRequestDtoCheck = itemRequestDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest1);
        when(itemRequestMapper.itemRequestToItemRequestDto(any(ItemRequest.class))).thenReturn(itemRequestDto1);

        ItemRequestDto createdItemRequestDto = itemRequestService.createItemRequest(itemRequest, user1.getId());

        assertNotNull(createdItemRequestDto);
        assertEquals(itemRequestDtoCheck.getId(), createdItemRequestDto.getId());
        assertEquals(itemRequestDtoCheck.getDescription(), createdItemRequestDto.getDescription());
        assertEquals(itemRequestDtoCheck.getCreated(), createdItemRequestDto.getCreated());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
        verify(itemRequestMapper, times(1)).itemRequestToItemRequestDto(any(ItemRequest.class));
    }

    @Test
    @DisplayName("createItemRequest_whenUserNotExisted_thenNotFoundExceptionThrown")
    public void createItemRequest_whenUserNotExisted_thenNotFoundExceptionThrown() {
        ItemRequest itemRequest = ItemRequest.builder().description("itemRequestDescription1").build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemRequestService.createItemRequest(itemRequest, 999L));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
        verify(itemRequestMapper, never()).itemRequestToItemRequestDto(any(ItemRequest.class));
    }

    @Test
    @DisplayName("getAllItemRequestsByRequestor_whenInvoked_thenListOfItemRequestInfoDtoReturned")
    public void getAllItemRequestsByRequestor_whenInvoked_thenListOfItemRequestInfoDtoReturned() {
        ItemRequestInfoDto itemRequestInfoDtoStandard1 = ItemRequestInfoDto.builder().id(1L)
                .description("itemRequestDescription1").created(itemRequestInfoDto1.getCreated())
                .items(List.of(itemFromItemRequestInfoDto1, itemFromItemRequestInfoDto2))
                .build();
        ItemRequestInfoDto itemRequestInfoDtoStandard2 = ItemRequestInfoDto.builder().id(2L)
                .description("itemRequestDescription2").created(itemRequestInfoDto2.getCreated())
                .items(List.of(itemFromItemRequestInfoDto3, itemFromItemRequestInfoDto4))
                .build();
        List<ItemRequestInfoDto> itemRequestInfoDtosStandard = List.of(itemRequestInfoDtoStandard1,
                itemRequestInfoDtoStandard2);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findRequestByRequestorIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(itemRequest1, itemRequest2));
        when(itemRepository.findAllByItemRequestIn(anyList())).thenReturn(List.of(item1, item2, item3, item4));
        when(itemRequestMapper.itemRequestToItemRequestInfoDto(any(ItemRequest.class)))
                .thenAnswer((invocationOnMock) -> {
                    ItemRequest itemRequest = invocationOnMock.getArgument(0);
                    if (Objects.equals(itemRequest.getId(), itemRequest1.getId())) {
                        return itemRequestInfoDto1;
                    } else if (Objects.equals(itemRequest.getId(), itemRequest2.getId())) {
                        return itemRequestInfoDto2;
                    }
                    return null;
                });
        when(itemMapper.itemToItemRequestInfoDtoItemDto(any(Item.class)))
                .thenAnswer((invocationOnMock) -> {
                    Item item = invocationOnMock.getArgument(0);
                    if (Objects.equals(item.getId(), item1.getId())) {
                        return itemFromItemRequestInfoDto1;
                    } else if (Objects.equals(item.getId(), item2.getId())) {
                        return itemFromItemRequestInfoDto2;
                    } else if (Objects.equals(item.getId(), item3.getId())) {
                        return itemFromItemRequestInfoDto3;
                    } else if (Objects.equals(item.getId(), item4.getId())) {
                        return itemFromItemRequestInfoDto4;
                    }
                    return null;
                });

        List<ItemRequestInfoDto> itemRequestInfoDtos = itemRequestService.getAllItemRequestsByRequestor(user1.getId());

        assertNotNull(itemRequestInfoDtos);
        assertFalse(itemRequestInfoDtos.isEmpty());
        assertEquals(2, itemRequestInfoDtos.size());
        assertNotNull(itemRequestInfoDtos.get(0));
        assertNotNull(itemRequestInfoDtos.get(1));
        assertFalse(itemRequestInfoDtos.get(0).getItems().isEmpty());
        assertFalse(itemRequestInfoDtos.get(1).getItems().isEmpty());
        assertEquals(itemRequestInfoDtosStandard, itemRequestInfoDtos);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1))
                .findRequestByRequestorIdOrderByCreatedDesc(anyLong());
        verify(itemRepository, times(1)).findAllByItemRequestIn(anyList());
        verify(itemRequestMapper, times(2))
                .itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, times(4)).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getAllItemRequestsByRequestor_whenNoResponses_thenListOfItemRequestInfoDtoWithoutResponsesReturned")
    public void getAllItemRequestsByRequestor_whenNoResponses_thenListOfItemRequestInfoDtoWithoutResponsesReturned() {
        ItemRequestInfoDto itemRequestInfoDtoStandard1 = ItemRequestInfoDto.builder().id(1L)
                .description("itemRequestDescription1").created(itemRequestInfoDto1.getCreated())
                .items(Collections.emptyList()).build();
        ItemRequestInfoDto itemRequestInfoDtoStandard2 = ItemRequestInfoDto.builder().id(2L)
                .description("itemRequestDescription2").created(itemRequestInfoDto2.getCreated())
                .items(Collections.emptyList()).build();
        List<ItemRequestInfoDto> itemRequestInfoDtosStandard = List.of(itemRequestInfoDtoStandard1,
                itemRequestInfoDtoStandard2);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findRequestByRequestorIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(itemRequest1, itemRequest2));
        when(itemRepository.findAllByItemRequestIn(anyList())).thenReturn(Collections.emptyList());
        when(itemRequestMapper.itemRequestToItemRequestInfoDto(any(ItemRequest.class)))
                .thenAnswer((invocationOnMock) -> {
                    ItemRequest itemRequest = invocationOnMock.getArgument(0);
                    if (Objects.equals(itemRequest.getId(), itemRequest1.getId())) {
                        return itemRequestInfoDto1;
                    } else if (Objects.equals(itemRequest.getId(), itemRequest2.getId())) {
                        return itemRequestInfoDto2;
                    }
                    return null;
                });

        List<ItemRequestInfoDto> itemRequestInfoDtos = itemRequestService.getAllItemRequestsByRequestor(user1.getId());

        assertNotNull(itemRequestInfoDtos);
        assertFalse(itemRequestInfoDtos.isEmpty());
        assertEquals(2, itemRequestInfoDtos.size());
        assertNotNull(itemRequestInfoDtos.get(0));
        assertNotNull(itemRequestInfoDtos.get(1));
        assertTrue(itemRequestInfoDtos.get(0).getItems().isEmpty());
        assertTrue(itemRequestInfoDtos.get(1).getItems().isEmpty());
        assertEquals(itemRequestInfoDtosStandard, itemRequestInfoDtos);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1))
                .findRequestByRequestorIdOrderByCreatedDesc(anyLong());
        verify(itemRepository, times(1)).findAllByItemRequestIn(anyList());
        verify(itemRequestMapper, times(2))
                .itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, never()).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getAllItemRequestsByRequestor_whenUserNotExisted_thenNotFoundExceptionThrown")
    public void getAllItemRequestsByRequestor_whenUserNotExisted_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemRequestService.getAllItemRequestsByRequestor(999L));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).findRequestByRequestorIdOrderByCreatedDesc(anyLong());
        verify(itemRepository, never()).findAllByItemRequestIn(anyList());
        verify(itemRequestMapper, never()).itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, never()).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getAllItemRequestsByRequestor_whenNoItemRequests_thenEmptyListReturned")
    public void getAllItemRequestsByRequestor_whenNoItemRequests_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findRequestByRequestorIdOrderByCreatedDesc(anyLong()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestInfoDto> itemRequestInfoDtos = itemRequestService.getAllItemRequestsByRequestor(user1.getId());

        assertNotNull(itemRequestInfoDtos);
        assertTrue(itemRequestInfoDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1))
                .findRequestByRequestorIdOrderByCreatedDesc(anyLong());
        verify(itemRepository, never()).findAllByItemRequestIn(anyList());
        verify(itemRequestMapper, never()).itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, never()).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getSeveralItemRequestsPaginated_whenInvoked_thenListOfItemRequestInfoDtoReturned")
    public void getSeveralItemRequestsPaginated_whenInvoked_thenListOfItemRequestInfoDtoReturned() {
        LocalDateTime created4 = LocalDateTime.now();
        LocalDateTime created5 = LocalDateTime.now();
        LocalDateTime created6 = LocalDateTime.now();
        LocalDateTime created7 = LocalDateTime.now();
        ItemRequest itemRequest4 = ItemRequest.builder().id(4L).description("itemRequestDescription4")
                .created(created4).requestor(user2).build();
        ItemRequest itemRequest5 = ItemRequest.builder().id(5L).description("itemRequestDescription5")
                .created(created5).requestor(user2).build();
        ItemRequest itemRequest6 = ItemRequest.builder().id(6L).description("itemRequestDescription6")
                .created(created6).requestor(user2).build();
        ItemRequest itemRequest7 = ItemRequest.builder().id(7L).description("itemRequestDescription7")
                .created(created7).requestor(user2).build();
        List<ItemRequest> pageItemRequests = List.of(itemRequest4,
                itemRequest5, itemRequest6, itemRequest7);
        Item item6 = Item.builder().id(6L).name("item6").description("itemDescription6")
                .available(Boolean.TRUE).owner(user2).itemRequest(itemRequest4).build();
        Item item7 = Item.builder().id(7L).name("item7").description("itemDescription7")
                .available(Boolean.TRUE).owner(user2).itemRequest(itemRequest5).build();
        Item item8 = Item.builder().id(8L).name("item8").description("itemDescription8")
                .available(Boolean.TRUE).owner(user2).itemRequest(itemRequest6).build();
        Item item9 = Item.builder().id(9L).name("item9").description("itemDescription9")
                .available(Boolean.TRUE).owner(user2).itemRequest(itemRequest7).build();
        List<Item> pageRequests = List.of(item6, item7, item8, item9);
        ItemRequestInfoDto itemRequestInfoDto4 = ItemRequestInfoDto.builder().id(4L)
                .description("itemRequestDescription4").created(created4).build();
        ItemRequestInfoDto itemRequestInfoDto5 = ItemRequestInfoDto.builder().id(5L)
                .description("itemRequestDescription5").created(created5).build();
        ItemRequestInfoDto itemRequestInfoDto6 = ItemRequestInfoDto.builder().id(6L)
                .description("itemRequestDescription6").created(created6).build();
        ItemRequestInfoDto itemRequestInfoDto7 = ItemRequestInfoDto.builder().id(7L)
                .description("itemRequestDescription7").created(created7).build();
        ItemRequestInfoDto.ItemDto itemFromItemRequestInfoDto6 = ItemRequestInfoDto.ItemDto.builder()
                .id(6L).name("item6").description("itemDescription6").available(Boolean.TRUE)
                .requestId(itemRequest4.getId()).build();
        ItemRequestInfoDto.ItemDto itemFromItemRequestInfoDto7 = ItemRequestInfoDto.ItemDto.builder()
                .id(7L).name("item7").description("itemDescription7").available(Boolean.TRUE)
                .requestId(itemRequest5.getId()).build();
        ItemRequestInfoDto.ItemDto itemFromItemRequestInfoDto8 = ItemRequestInfoDto.ItemDto.builder()
                .id(8L).name("item8").description("itemDescription8").available(Boolean.TRUE)
                .requestId(itemRequest6.getId()).build();
        ItemRequestInfoDto.ItemDto itemFromItemRequestInfoDto9 = ItemRequestInfoDto.ItemDto.builder()
                .id(9L).name("item9").description("itemDescription9").available(Boolean.TRUE)
                .requestId(itemRequest7.getId()).build();
        ItemRequestInfoDto itemRequestInfoDtoWithRResponses4 = ItemRequestInfoDto.builder()
                .id(itemRequestInfoDto4.getId()).description(itemRequestInfoDto4.getDescription())
                .created(itemRequestInfoDto4.getCreated()).items(List.of(itemFromItemRequestInfoDto6)).build();
        ItemRequestInfoDto itemRequestInfoDtoWithRResponses5 = ItemRequestInfoDto.builder()
                .id(itemRequestInfoDto5.getId()).description(itemRequestInfoDto5.getDescription())
                .created(itemRequestInfoDto5.getCreated()).items(List.of(itemFromItemRequestInfoDto7)).build();
        ItemRequestInfoDto itemRequestInfoDtoWithRResponses6 = ItemRequestInfoDto.builder()
                .id(itemRequestInfoDto6.getId()).description(itemRequestInfoDto6.getDescription())
                .created(itemRequestInfoDto6.getCreated()).items(List.of(itemFromItemRequestInfoDto8)).build();
        ItemRequestInfoDto itemRequestInfoDtoWithRResponses7 = ItemRequestInfoDto.builder()
                .id(itemRequestInfoDto7.getId()).description(itemRequestInfoDto7.getDescription())
                .created(itemRequestInfoDto7.getCreated()).items(List.of(itemFromItemRequestInfoDto9)).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findAllWithoutRequestor(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(pageItemRequests));
        when(itemRepository.findAllByItemRequestIn(anyList())).thenReturn(pageRequests);
        when(itemRequestMapper.itemRequestToItemRequestInfoDto(any(ItemRequest.class)))
                .thenAnswer((invocationOnMock) -> {
                    ItemRequest itemRequest = invocationOnMock.getArgument(0);
                    if (Objects.equals(itemRequest.getId(), itemRequest4.getId())) {
                        return itemRequestInfoDto4;
                    } else if (Objects.equals(itemRequest.getId(), itemRequest5.getId())) {
                        return itemRequestInfoDto5;
                    } else if (Objects.equals(itemRequest.getId(), itemRequest6.getId())) {
                        return itemRequestInfoDto6;
                    } else if (Objects.equals(itemRequest.getId(), itemRequest7.getId())) {
                        return itemRequestInfoDto7;
                    }
                    return null;
                });
        when(itemMapper.itemToItemRequestInfoDtoItemDto(any(Item.class)))
                .thenAnswer((invocationOnMock) -> {
                    Item item = invocationOnMock.getArgument(0);
                    if (Objects.equals(item.getId(), item6.getId())) {
                        return itemFromItemRequestInfoDto6;
                    } else if (Objects.equals(item.getId(), item7.getId())) {
                        return itemFromItemRequestInfoDto7;
                    } else if (Objects.equals(item.getId(), item8.getId())) {
                        return itemFromItemRequestInfoDto8;
                    } else if (Objects.equals(item.getId(), item9.getId())) {
                        return itemFromItemRequestInfoDto9;
                    }
                    return null;
                });

        List<ItemRequestInfoDto> itemRequestInfoDtos = itemRequestService.getSeveralItemRequestsPaginated(
                1, 4, user1.getId());

        assertNotNull(itemRequestInfoDtos);
        assertFalse(itemRequestInfoDtos.isEmpty());
        assertEquals(4, itemRequestInfoDtos.size());
        assertNotNull(itemRequestInfoDtos.get(0));
        assertNotNull(itemRequestInfoDtos.get(1));
        assertNotNull(itemRequestInfoDtos.get(2));
        assertNotNull(itemRequestInfoDtos.get(3));
        assertFalse(itemRequestInfoDtos.get(0).getItems().isEmpty());
        assertFalse(itemRequestInfoDtos.get(1).getItems().isEmpty());
        assertFalse(itemRequestInfoDtos.get(2).getItems().isEmpty());
        assertFalse(itemRequestInfoDtos.get(3).getItems().isEmpty());
        assertEquals(itemRequestInfoDtoWithRResponses4, itemRequestInfoDtos.get(0));
        assertEquals(itemRequestInfoDtoWithRResponses5, itemRequestInfoDtos.get(1));
        assertEquals(itemRequestInfoDtoWithRResponses6, itemRequestInfoDtos.get(2));
        assertEquals(itemRequestInfoDtoWithRResponses7, itemRequestInfoDtos.get(3));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1))
                .findAllWithoutRequestor(anyLong(), any(Pageable.class));
        verify(itemRepository, times(1)).findAllByItemRequestIn(anyList());
        verify(itemRequestMapper, times(4))
                .itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, times(4)).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getSeveralItemRequestsPaginated_whenNoResponses_thenListOfItemRequestInfoDtoWithoutResponsesReturned")
    public void getSeveralItemRequestsPaginated_whenNoResponses_thenListOfItemRequestInfoDtoWithoutResponsesReturned() {
        LocalDateTime created4 = LocalDateTime.now();
        LocalDateTime created5 = LocalDateTime.now();
        LocalDateTime created6 = LocalDateTime.now();
        LocalDateTime created7 = LocalDateTime.now();
        ItemRequest itemRequest4 = ItemRequest.builder().id(4L).description("itemRequestDescription4")
                .created(created4).requestor(user2).build();
        ItemRequest itemRequest5 = ItemRequest.builder().id(5L).description("itemRequestDescription5")
                .created(created5).requestor(user2).build();
        ItemRequest itemRequest6 = ItemRequest.builder().id(6L).description("itemRequestDescription6")
                .created(created6).requestor(user2).build();
        ItemRequest itemRequest7 = ItemRequest.builder().id(7L).description("itemRequestDescription7")
                .created(created7).requestor(user2).build();
        List<ItemRequest> pageItemRequests = List.of(itemRequest4,
                itemRequest5, itemRequest6, itemRequest7);
        ItemRequestInfoDto itemRequestInfoDto4 = ItemRequestInfoDto.builder().id(4L)
                .description("itemRequestDescription4").created(created4).build();
        ItemRequestInfoDto itemRequestInfoDto5 = ItemRequestInfoDto.builder().id(5L)
                .description("itemRequestDescription5").created(created5).build();
        ItemRequestInfoDto itemRequestInfoDto6 = ItemRequestInfoDto.builder().id(6L)
                .description("itemRequestDescription6").created(created6).build();
        ItemRequestInfoDto itemRequestInfoDto7 = ItemRequestInfoDto.builder().id(7L)
                .description("itemRequestDescription7").created(created7).build();
        ItemRequestInfoDto itemRequestInfoDtoWithRResponses4 = ItemRequestInfoDto.builder()
                .id(itemRequestInfoDto4.getId()).description(itemRequestInfoDto4.getDescription())
                .created(itemRequestInfoDto4.getCreated()).items(Collections.emptyList()).build();
        ItemRequestInfoDto itemRequestInfoDtoWithRResponses5 = ItemRequestInfoDto.builder()
                .id(itemRequestInfoDto5.getId()).description(itemRequestInfoDto5.getDescription())
                .created(itemRequestInfoDto5.getCreated()).items(Collections.emptyList()).build();
        ItemRequestInfoDto itemRequestInfoDtoWithRResponses6 = ItemRequestInfoDto.builder()
                .id(itemRequestInfoDto6.getId()).description(itemRequestInfoDto6.getDescription())
                .created(itemRequestInfoDto6.getCreated()).items(Collections.emptyList()).build();
        ItemRequestInfoDto itemRequestInfoDtoWithRResponses7 = ItemRequestInfoDto.builder()
                .id(itemRequestInfoDto7.getId()).description(itemRequestInfoDto7.getDescription())
                .created(itemRequestInfoDto7.getCreated()).items(Collections.emptyList()).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findAllWithoutRequestor(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(pageItemRequests));
        when(itemRepository.findAllByItemRequestIn(anyList())).thenReturn(Collections.emptyList());
        when(itemRequestMapper.itemRequestToItemRequestInfoDto(any(ItemRequest.class)))
                .thenAnswer((invocationOnMock) -> {
                    ItemRequest itemRequest = invocationOnMock.getArgument(0);
                    if (Objects.equals(itemRequest.getId(), itemRequest4.getId())) {
                        return itemRequestInfoDto4;
                    } else if (Objects.equals(itemRequest.getId(), itemRequest5.getId())) {
                        return itemRequestInfoDto5;
                    } else if (Objects.equals(itemRequest.getId(), itemRequest6.getId())) {
                        return itemRequestInfoDto6;
                    } else if (Objects.equals(itemRequest.getId(), itemRequest7.getId())) {
                        return itemRequestInfoDto7;
                    }
                    return null;
                });

        List<ItemRequestInfoDto> itemRequestInfoDtos = itemRequestService.getSeveralItemRequestsPaginated(
                1, 4, user1.getId());

        assertNotNull(itemRequestInfoDtos);
        assertFalse(itemRequestInfoDtos.isEmpty());
        assertEquals(4, itemRequestInfoDtos.size());
        assertNotNull(itemRequestInfoDtos.get(0));
        assertNotNull(itemRequestInfoDtos.get(1));
        assertNotNull(itemRequestInfoDtos.get(2));
        assertNotNull(itemRequestInfoDtos.get(3));
        assertTrue(itemRequestInfoDtos.get(0).getItems().isEmpty());
        assertTrue(itemRequestInfoDtos.get(1).getItems().isEmpty());
        assertTrue(itemRequestInfoDtos.get(2).getItems().isEmpty());
        assertTrue(itemRequestInfoDtos.get(3).getItems().isEmpty());
        assertEquals(itemRequestInfoDtoWithRResponses4, itemRequestInfoDtos.get(0));
        assertEquals(itemRequestInfoDtoWithRResponses5, itemRequestInfoDtos.get(1));
        assertEquals(itemRequestInfoDtoWithRResponses6, itemRequestInfoDtos.get(2));
        assertEquals(itemRequestInfoDtoWithRResponses7, itemRequestInfoDtos.get(3));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1))
                .findAllWithoutRequestor(anyLong(), any(Pageable.class));
        verify(itemRepository, times(1)).findAllByItemRequestIn(anyList());
        verify(itemRequestMapper, times(4))
                .itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, never()).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getSeveralItemRequestsPaginated_whenNoItemRequests_thenEmptyListReturned")
    public void getSeveralItemRequestsPaginated_whenNoItemRequests_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findAllWithoutRequestor(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<ItemRequestInfoDto> itemRequestInfoDtos = itemRequestService.getSeveralItemRequestsPaginated(
                1, 4, user1.getId());

        assertNotNull(itemRequestInfoDtos);
        assertTrue(itemRequestInfoDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1))
                .findAllWithoutRequestor(anyLong(), any(Pageable.class));
        verify(itemRepository, never()).findAllByItemRequestIn(anyList());
        verify(itemRequestMapper, never()).itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, never()).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getSeveralItemRequestsPaginated_whenNoItemRequests_thenEmptyListReturned")
    public void getSeveralItemRequestsPaginated_whenRequestorNotFound_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException notFoundException = assertThrows(NotFoundException.class, () ->
                itemRequestService.getSeveralItemRequestsPaginated(1, 4, 999L));

        assertEquals("User with id 999 not found", notFoundException.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).findAllWithoutRequestor(anyLong(), any(Pageable.class));
        verify(itemRepository, never()).findAllByItemRequestIn(anyList());
        verify(itemRequestMapper, never()).itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, never()).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getItemRequestById_whenInvoked_thenListOfItemRequestInfoDtoReturned")
    public void getItemRequestById_whenInvoked_thenListOfItemRequestInfoDtoReturned() {
        ItemRequestInfoDto itemRequestInfoDtoWithRResponses = ItemRequestInfoDto.builder()
                .id(itemRequestInfoDto1.getId()).description(itemRequestInfoDto1.getDescription())
                .created(itemRequestInfoDto1.getCreated())
                .items(List.of(itemFromItemRequestInfoDto1, itemFromItemRequestInfoDto2)).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest1));
        when(itemRepository.findAllByItemRequest(any(ItemRequest.class))).thenReturn(List.of(item1, item2));
        when(itemRequestMapper.itemRequestToItemRequestInfoDto(any(ItemRequest.class)))
                .thenReturn(itemRequestInfoDto1);
        when(itemMapper.itemToItemRequestInfoDtoItemDto(any(Item.class)))
                .thenAnswer((invocationOnMock) -> {
                    Item item = invocationOnMock.getArgument(0);
                    if (Objects.equals(item.getId(), item1.getId())) {
                        return itemFromItemRequestInfoDto1;
                    } else if (Objects.equals(item.getId(), item2.getId())) {
                        return itemFromItemRequestInfoDto2;
                    }
                    return null;
                });

        ItemRequestInfoDto itemRequestInfoDto = itemRequestService.getItemRequestById(itemRequest1.getId(),
                user1.getId());

        assertNotNull(itemRequestInfoDto);
        assertFalse(itemRequestInfoDto.getItems().isEmpty());
        assertEquals(itemRequestInfoDtoWithRResponses, itemRequestInfoDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByItemRequest(any(ItemRequest.class));
        verify(itemRequestMapper, times(1))
                .itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, times(2)).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getItemRequestById_whenNoResponses_thenListOfItemRequestInfoDtoWithoutResponsesReturned")
    public void getItemRequestById_whenNoResponses_thenListOfItemRequestInfoDtoWithoutResponsesReturned() {
        ItemRequestInfoDto itemRequestInfoDtoWithRResponses = ItemRequestInfoDto.builder()
                .id(itemRequestInfoDto1.getId()).description(itemRequestInfoDto1.getDescription())
                .created(itemRequestInfoDto1.getCreated())
                .items(Collections.emptyList()).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest1));
        when(itemRepository.findAllByItemRequest(any(ItemRequest.class))).thenReturn(Collections.emptyList());
        when(itemRequestMapper.itemRequestToItemRequestInfoDto(any(ItemRequest.class)))
                .thenReturn(itemRequestInfoDto1);

        ItemRequestInfoDto itemRequestInfoDto = itemRequestService.getItemRequestById(itemRequest1.getId(),
                user1.getId());

        assertNotNull(itemRequestInfoDto);
        assertTrue(itemRequestInfoDto.getItems().isEmpty());
        assertEquals(itemRequestInfoDtoWithRResponses, itemRequestInfoDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByItemRequest(any(ItemRequest.class));
        verify(itemRequestMapper, times(1))
                .itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, never()).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getItemRequestById_whenNoRequests_thenNotFoundExceptionThrown")
    public void getItemRequestById_whenNoRequests_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemRequestService.getItemRequestById(999L, user1.getId()));

        assertEquals("Item request with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findAllByItemRequest(any(ItemRequest.class));
        verify(itemRequestMapper, never()).itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, never()).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getItemRequestById_whenUserNotfound_thenNotFoundExceptionThrown")
    public void getItemRequestById_whenUserNotfound_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemRequestService.getItemRequestById(itemRequest1.getId(), 999L));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findAllByItemRequest(any(ItemRequest.class));
        verify(itemRequestMapper, never()).itemRequestToItemRequestInfoDto(any(ItemRequest.class));
        verify(itemMapper, never()).itemToItemRequestInfoDtoItemDto(any(Item.class));
    }
}
