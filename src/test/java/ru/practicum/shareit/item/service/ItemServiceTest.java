package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.util.mapper.BookingMapper;
import ru.practicum.shareit.exception.AccessToAddCommentDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.util.mapper.CommentMapper;
import ru.practicum.shareit.item.util.mapper.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    ItemMapper itemMapper;
    @Mock
    BookingMapper bookingMapper;
    @Mock
    CommentMapper commentMapper;
    @InjectMocks
    ItemServiceImpl itemService;
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
        item1 = Item.builder().id(1L).name("item1").description("itemDescription1")
                .available(Boolean.TRUE).owner(user1).itemRequest(itemRequest1).build();
        itemDto1 = ItemDto.builder().id(1L).name("item1").description("description1")
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
        commentDto1 = CommentDto.builder().id(1L).text("comment1").created(start1.plusDays(5))
                .authorName(user2.getName()).build();
        commentDto2 = CommentDto.builder().id(2L).text("comment2").created(start1.plusDays(7))
                .authorName(user2.getName()).build();
        itemInfoDto1 = ItemInfoDto.builder().id(1L).name("item1").description("itemDescription1")
                .available(Boolean.TRUE).build();
    }

    @Test
    @DisplayName("createItem_whenInvoked_thenSavedItemDtoWithIdReturned")
    void createItem_whenInvoked_thenSavedItemDtoWithIdReturned() {
        Item itemToCreate = Item.builder().name("item1").description("itemDescription1")
                .available(Boolean.TRUE).build();
        ItemDto itemDtoCheck = itemDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest1));
        when(itemRepository.save(any(Item.class))).thenReturn(item1);
        when(itemMapper.itemToItemDto(any(Item.class))).thenReturn(itemDto1);

        ItemDto itemDto = itemService.createItem(itemToCreate, user1.getId(), itemRequest1.getId());

        assertNotNull(itemDto);
        assertEquals(itemDtoCheck, itemDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemMapper, times(1)).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("createItem_whenRequestIdNotFound_thenNotFoundExceptionThrown")
    void createItem_whenRequestIdNotFound_thenNotFoundExceptionThrown() {
        Item itemToCreate = Item.builder().name("item1").description("itemDescription1")
                .available(Boolean.TRUE).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.createItem(itemToCreate, user1.getId(), 999L));

        assertEquals("Item request with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
        verify(itemMapper, never()).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("createItem_whenInvokedWithoutRequestId_thenSavedItemDtoWithIdReturned")
    void createItem_whenInvokedWithoutRequestId_thenSavedItemDtoWithIdReturned() {
        Item itemToCreate = Item.builder().name("item1").description("itemDescription1")
                .available(Boolean.TRUE).build();
        item1.setItemRequest(null);
        itemDto1.setRequestId(null);
        ItemDto itemDtoCheck = itemDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.save(any(Item.class))).thenReturn(item1);
        when(itemMapper.itemToItemDto(any(Item.class))).thenReturn(itemDto1);

        ItemDto itemDto = itemService.createItem(itemToCreate, user1.getId(), null);

        assertNotNull(itemDto);
        assertEquals(itemDtoCheck, itemDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).findById(anyLong());
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemMapper, times(1)).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("createItem_whenUserNotFound_thenNotFoundExceptionThrown")
    void createItem_whenUserNotFound_thenNotFoundExceptionThrown() {
        Item itemToCreate = Item.builder().name("item1").description("itemDescription1")
                .available(Boolean.TRUE).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.createItem(itemToCreate, 999L, itemRequest1.getId()));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
        verify(itemMapper, never()).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("getAllUserItems_whenInvoked_thenListOfItemInfoDtoReturned")
    void getAllUserItems_whenInvoked_thenListOfItemInfoDtoReturned() {
        Item item2 = Item.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).owner(user1).build();
        LocalDateTime start2 = LocalDateTime.now();
        Booking booking3 = Booking.builder().id(3L).status(BookingStatus.PAST).start(start2.minusDays(2))
                .end(start2.minusDays(1)).booker(user2).item(item2).build();
        Booking booking4 = Booking.builder().id(4L).status(BookingStatus.FUTURE).start(start2.plusDays(1))
                .end(start2.plusDays(2)).booker(user2).item(item2).build();
        Booking bookingNotLast2 = Booking.builder().id(21L).status(BookingStatus.PAST).start(start2.minusDays(5))
                .end(start2.minusDays(4)).booker(user2).item(item2).build();
        Booking bookingNotNext2 = Booking.builder().id(22L).status(BookingStatus.FUTURE).start(start2.plusDays(4))
                .end(start2.plusDays(5)).booker(user2).item(item2).build();
        ItemInfoDto.BookingDto itemInfoDtoBookingDto3 = ItemInfoDto.BookingDto.builder().id(3L)
                .status(BookingStatus.PAST).start(start2.minusDays(2)).end(start2.minusDays(1))
                .bookerId(user2.getId()).itemId(item2.getId()).build();
        ItemInfoDto.BookingDto itemInfoDtoBookingDto4 = ItemInfoDto.BookingDto.builder().id(4L)
                .status(BookingStatus.PAST).start(start2.plusDays(1)).end(start2.plusDays(2))
                .bookerId(user2.getId()).itemId(item2.getId()).build();
        ItemInfoDto.BookingDto itemInfoDtoBookingDtoNotLast2 = ItemInfoDto.BookingDto.builder().id(21L)
                .status(BookingStatus.PAST).start(start2.minusDays(5)).end(start2.minusDays(4))
                .bookerId(user2.getId()).itemId(item2.getId()).build();
        ItemInfoDto.BookingDto itemInfoDtoBookingDtoNotNext2 = ItemInfoDto.BookingDto.builder().id(22L)
                .status(BookingStatus.PAST).start(start2.plusDays(4)).end(start2.plusDays(5))
                .bookerId(user2.getId()).itemId(item2.getId()).build();
        Comment comment3 = Comment.builder().id(3L).text("comment3").item(item2).author(user2)
                .created(start2.plusDays(5)).build();
        Comment comment4 = Comment.builder().id(4L).text("comment4").item(item2).author(user2)
                .created(start2.plusDays(7)).build();
        CommentDto commentDto3 = CommentDto.builder().id(3L).text("comment3").created(start2.plusDays(5))
                .authorName(user2.getName()).build();
        CommentDto commentDto4 = CommentDto.builder().id(4L).text("comment4").created(start2.plusDays(7))
                .authorName(user2.getName()).build();
        ItemInfoDto itemInfoDto2 = ItemInfoDto.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).build();
        ItemInfoDto itemInfoDtoCheck1 = itemInfoDto1.toBuilder().lastBooking(itemInfoDtoBookingDto1)
                .nextBooking(itemInfoDtoBookingDto2).comments(List.of(commentDto1, commentDto2)).build();
        ItemInfoDto itemInfoDtoCheck2 = itemInfoDto2.toBuilder().lastBooking(itemInfoDtoBookingDto3)
                .nextBooking(itemInfoDtoBookingDto4).comments(List.of(commentDto3, commentDto4)).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findAllByOwnerIdOrderById(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item1, item2)));
        when(bookingRepository.findUserLastBookingsForEachItem(anyLong()))
                .thenReturn(List.of(booking1, bookingNotLast1, booking3, bookingNotLast2));
        when(bookingMapper.bookingToItemInfoDtoBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return itemInfoDtoBookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return itemInfoDtoBookingDto2;
            } else if (Objects.equals(booking, booking3)) {
                return itemInfoDtoBookingDto3;
            } else if (Objects.equals(booking, booking4)) {
                return itemInfoDtoBookingDto4;
            } else if (Objects.equals(booking, bookingNotLast1)) {
                return itemInfoDtoBookingDtoNotLast1;
            } else if (Objects.equals(booking, bookingNotLast2)) {
                return itemInfoDtoBookingDtoNotLast2;
            } else if (Objects.equals(booking, bookingNotNext1)) {
                return itemInfoDtoBookingDtoNotNext1;
            } else if (Objects.equals(booking, bookingNotNext2)) {
                return itemInfoDtoBookingDtoNotNext2;
            }
            return null;
        });
        when(bookingRepository.findUserNextBookingsForEachItem(anyLong()))
                .thenReturn(List.of(booking2, bookingNotNext1, booking4, bookingNotNext2));
        when(commentRepository.findAllCommentsLeftOnOwnerItemsWithId(anyLong())).thenReturn(List.of(comment1, comment2,
                comment3, comment4));
        when(commentMapper.commentToCommentDto(any(Comment.class))).thenAnswer((invocationOnMock) -> {
            Comment comment = invocationOnMock.getArgument(0);
            if (Objects.equals(comment, comment1)) {
                return commentDto1;
            } else if (Objects.equals(comment, comment2)) {
                return commentDto2;
            } else if (Objects.equals(comment, comment3)) {
                return commentDto3;
            } else if (Objects.equals(comment, comment4)) {
                return commentDto4;
            }
            return null;
        });
        when(itemMapper.itemToItemInfoDto(any(Item.class))).thenAnswer((invocationOnMock) -> {
            Item item = invocationOnMock.getArgument(0);
            if (Objects.equals(item, item1)) {
                return itemInfoDto1;
            } else if (Objects.equals(item, item2)) {
                return itemInfoDto2;
            }
            return null;
        });

        List<ItemInfoDto> itemInfoDtos = itemService.getAllUserItems(user1.getId(), 1, 2);

        assertNotNull(itemInfoDtos);
        assertFalse(itemInfoDtos.isEmpty());
        assertEquals(2, itemInfoDtos.size());
        assertNotNull(itemInfoDtos.get(0));
        assertNotNull(itemInfoDtos.get(1));
        assertEquals(itemInfoDtoCheck1, itemInfoDtos.get(0));
        assertEquals(itemInfoDtoCheck2, itemInfoDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1))
                .findAllByOwnerIdOrderById(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1)).findUserLastBookingsForEachItem(anyLong());
        verify(bookingMapper, times(8)).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(bookingRepository, times(1)).findUserLastBookingsForEachItem(anyLong());
        verify(commentRepository, times(1)).findAllCommentsLeftOnOwnerItemsWithId(anyLong());
        verify(commentMapper, times(4)).commentToCommentDto(any(Comment.class));
        verify(itemMapper, times(2)).itemToItemInfoDto(any(Item.class));
    }

    @Test
    @DisplayName("getAllUserItems_whenNoComments_thenListOfItemInfoDtoWithoutCommentsReturned")
    void getAllUserItems_whenNoComments_thenListOfItemInfoDtoWithoutCommentsReturned() {
        Item item2 = Item.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).owner(user1).build();
        LocalDateTime start2 = LocalDateTime.now();
        Booking booking3 = Booking.builder().id(3L).status(BookingStatus.PAST).start(start2.minusDays(2))
                .end(start2.minusDays(1)).booker(user2).item(item2).build();
        Booking booking4 = Booking.builder().id(4L).status(BookingStatus.FUTURE).start(start2.plusDays(1))
                .end(start2.plusDays(2)).booker(user2).item(item2).build();
        ItemInfoDto.BookingDto itemInfoDtoBookingDto3 = ItemInfoDto.BookingDto.builder().id(3L)
                .status(BookingStatus.PAST).start(start2.minusDays(2)).end(start2.minusDays(1))
                .bookerId(user2.getId()).itemId(item2.getId()).build();
        ItemInfoDto.BookingDto itemInfoDtoBookingDto4 = ItemInfoDto.BookingDto.builder().id(4L)
                .status(BookingStatus.PAST).start(start2.plusDays(1)).end(start2.plusDays(2))
                .bookerId(user2.getId()).itemId(item2.getId()).build();
        ItemInfoDto itemInfoDto2 = ItemInfoDto.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).build();
        ItemInfoDto itemInfoDtoCheck1 = itemInfoDto1.toBuilder().lastBooking(itemInfoDtoBookingDto1)
                .nextBooking(itemInfoDtoBookingDto2).comments(Collections.emptyList()).build();
        ItemInfoDto itemInfoDtoCheck2 = itemInfoDto2.toBuilder().lastBooking(itemInfoDtoBookingDto3)
                .nextBooking(itemInfoDtoBookingDto4).comments(Collections.emptyList()).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findAllByOwnerIdOrderById(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item1, item2)));
        when(bookingRepository.findUserLastBookingsForEachItem(anyLong())).thenReturn(List.of(booking1, booking3));
        when(bookingMapper.bookingToItemInfoDtoBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return itemInfoDtoBookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return itemInfoDtoBookingDto2;
            } else if (Objects.equals(booking, booking3)) {
                return itemInfoDtoBookingDto3;
            } else if (Objects.equals(booking, booking4)) {
                return itemInfoDtoBookingDto4;
            }
            return null;
        });
        when(bookingRepository.findUserNextBookingsForEachItem(anyLong())).thenReturn(List.of(booking2, booking4));
        when(commentRepository.findAllCommentsLeftOnOwnerItemsWithId(anyLong())).thenReturn(Collections.emptyList());
        when(itemMapper.itemToItemInfoDto(any(Item.class))).thenAnswer((invocationOnMock) -> {
            Item item = invocationOnMock.getArgument(0);
            if (Objects.equals(item, item1)) {
                return itemInfoDto1;
            } else if (Objects.equals(item, item2)) {
                return itemInfoDto2;
            }
            return null;
        });

        List<ItemInfoDto> itemInfoDtos = itemService.getAllUserItems(user1.getId(), 1, 2);

        assertNotNull(itemInfoDtos);
        assertFalse(itemInfoDtos.isEmpty());
        assertEquals(2, itemInfoDtos.size());
        assertNotNull(itemInfoDtos.get(0));
        assertNotNull(itemInfoDtos.get(1));
        assertEquals(itemInfoDtoCheck1, itemInfoDtos.get(0));
        assertEquals(itemInfoDtoCheck2, itemInfoDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1))
                .findAllByOwnerIdOrderById(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1)).findUserLastBookingsForEachItem(anyLong());
        verify(bookingMapper, times(4)).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(bookingRepository, times(1)).findUserLastBookingsForEachItem(anyLong());
        verify(commentRepository, times(1)).findAllCommentsLeftOnOwnerItemsWithId(anyLong());
        verify(commentMapper, never()).commentToCommentDto(any(Comment.class));
        verify(itemMapper, times(2)).itemToItemInfoDto(any(Item.class));
    }

    @Test
    @DisplayName("getAllUserItems_whenNoNextBooking_thenListOfItemInfoDtoWithoutNextBookingReturned")
    void getAllUserItems_whenNoNextBooking_thenListOfItemInfoDtoWithoutNextBookingReturned() {
        Item item2 = Item.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).owner(user1).build();
        LocalDateTime start2 = LocalDateTime.now();
        Booking booking3 = Booking.builder().id(3L).status(BookingStatus.PAST).start(start2.minusDays(2))
                .end(start2.minusDays(1)).booker(user2).item(item2).build();
        ItemInfoDto.BookingDto itemInfoDtoBookingDto3 = ItemInfoDto.BookingDto.builder().id(3L)
                .status(BookingStatus.PAST).start(start2.minusDays(2)).end(start2.minusDays(1))
                .bookerId(user2.getId()).itemId(item2.getId()).build();
        Comment comment3 = Comment.builder().id(3L).text("comment3").item(item2).author(user2)
                .created(start2.plusDays(5)).build();
        Comment comment4 = Comment.builder().id(4L).text("comment4").item(item2).author(user2)
                .created(start2.plusDays(7)).build();
        CommentDto commentDto3 = CommentDto.builder().id(3L).text("comment3").created(start2.plusDays(5))
                .authorName(user2.getName()).build();
        CommentDto commentDto4 = CommentDto.builder().id(4L).text("comment4").created(start2.plusDays(7))
                .authorName(user2.getName()).build();
        ItemInfoDto itemInfoDto2 = ItemInfoDto.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).build();
        ItemInfoDto itemInfoDtoCheck1 = itemInfoDto1.toBuilder().lastBooking(itemInfoDtoBookingDto1)
                .comments(List.of(commentDto1, commentDto2)).build();
        ItemInfoDto itemInfoDtoCheck2 = itemInfoDto2.toBuilder().lastBooking(itemInfoDtoBookingDto3)
                .comments(List.of(commentDto3, commentDto4)).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findAllByOwnerIdOrderById(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item1, item2)));
        when(bookingRepository.findUserLastBookingsForEachItem(anyLong())).thenReturn(List.of(booking1, booking3));
        when(bookingMapper.bookingToItemInfoDtoBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return itemInfoDtoBookingDto1;
            } else if (Objects.equals(booking, booking3)) {
                return itemInfoDtoBookingDto3;
            }
            return null;
        });
        when(bookingRepository.findUserNextBookingsForEachItem(anyLong())).thenReturn(Collections.emptyList());
        when(commentRepository.findAllCommentsLeftOnOwnerItemsWithId(anyLong())).thenReturn(List.of(comment1, comment2,
                comment3, comment4));
        when(commentMapper.commentToCommentDto(any(Comment.class))).thenAnswer((invocationOnMock) -> {
            Comment comment = invocationOnMock.getArgument(0);
            if (Objects.equals(comment, comment1)) {
                return commentDto1;
            } else if (Objects.equals(comment, comment2)) {
                return commentDto2;
            } else if (Objects.equals(comment, comment3)) {
                return commentDto3;
            } else if (Objects.equals(comment, comment4)) {
                return commentDto4;
            }
            return null;
        });
        when(itemMapper.itemToItemInfoDto(any(Item.class))).thenAnswer((invocationOnMock) -> {
            Item item = invocationOnMock.getArgument(0);
            if (Objects.equals(item, item1)) {
                return itemInfoDto1;
            } else if (Objects.equals(item, item2)) {
                return itemInfoDto2;
            }
            return null;
        });

        List<ItemInfoDto> itemInfoDtos = itemService.getAllUserItems(user1.getId(), 1, 2);

        assertNotNull(itemInfoDtos);
        assertFalse(itemInfoDtos.isEmpty());
        assertEquals(2, itemInfoDtos.size());
        assertNotNull(itemInfoDtos.get(0));
        assertNotNull(itemInfoDtos.get(1));
        assertEquals(itemInfoDtoCheck1, itemInfoDtos.get(0));
        assertEquals(itemInfoDtoCheck2, itemInfoDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1))
                .findAllByOwnerIdOrderById(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1)).findUserLastBookingsForEachItem(anyLong());
        verify(bookingMapper, times(2)).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(bookingRepository, times(1)).findUserLastBookingsForEachItem(anyLong());
        verify(commentRepository, times(1)).findAllCommentsLeftOnOwnerItemsWithId(anyLong());
        verify(commentMapper, times(4)).commentToCommentDto(any(Comment.class));
        verify(itemMapper, times(2)).itemToItemInfoDto(any(Item.class));
    }

    @Test
    @DisplayName("getAllUserItems_whenNoLastBookings_thenListOfItemInfoDtoWithoutLastBookingsReturned")
    void getAllUserItems_whenNoLastBookings_thenListOfItemInfoDtoWithoutLastBookingsReturned() {
        Item item2 = Item.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).owner(user1).build();
        LocalDateTime start2 = LocalDateTime.now();
        Booking booking4 = Booking.builder().id(4L).status(BookingStatus.FUTURE).start(start2.plusDays(1))
                .end(start2.plusDays(2)).booker(user2).item(item2).build();
        ItemInfoDto.BookingDto itemInfoDtoBookingDto4 = ItemInfoDto.BookingDto.builder().id(4L)
                .status(BookingStatus.PAST).start(start2.plusDays(1)).end(start2.plusDays(2))
                .bookerId(user2.getId()).itemId(item2.getId()).build();
        Comment comment3 = Comment.builder().id(3L).text("comment3").item(item2).author(user2)
                .created(start2.plusDays(5)).build();
        Comment comment4 = Comment.builder().id(4L).text("comment4").item(item2).author(user2)
                .created(start2.plusDays(7)).build();
        CommentDto commentDto3 = CommentDto.builder().id(3L).text("comment3").created(start2.plusDays(5))
                .authorName(user2.getName()).build();
        CommentDto commentDto4 = CommentDto.builder().id(4L).text("comment4").created(start2.plusDays(7))
                .authorName(user2.getName()).build();
        ItemInfoDto itemInfoDto2 = ItemInfoDto.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).build();
        ItemInfoDto itemInfoDtoCheck1 = itemInfoDto1.toBuilder()
                .nextBooking(itemInfoDtoBookingDto2).comments(List.of(commentDto1, commentDto2)).build();
        ItemInfoDto itemInfoDtoCheck2 = itemInfoDto2.toBuilder()
                .nextBooking(itemInfoDtoBookingDto4).comments(List.of(commentDto3, commentDto4)).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findAllByOwnerIdOrderById(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item1, item2)));
        when(bookingRepository.findUserLastBookingsForEachItem(anyLong())).thenReturn(Collections.emptyList());
        when(bookingMapper.bookingToItemInfoDtoBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking2)) {
                return itemInfoDtoBookingDto2;
            } else if (Objects.equals(booking, booking4)) {
                return itemInfoDtoBookingDto4;
            }
            return null;
        });
        when(bookingRepository.findUserNextBookingsForEachItem(anyLong())).thenReturn(List.of(booking2, booking4));
        when(commentRepository.findAllCommentsLeftOnOwnerItemsWithId(anyLong())).thenReturn(List.of(comment1, comment2,
                comment3, comment4));
        when(commentMapper.commentToCommentDto(any(Comment.class))).thenAnswer((invocationOnMock) -> {
            Comment comment = invocationOnMock.getArgument(0);
            if (Objects.equals(comment, comment1)) {
                return commentDto1;
            } else if (Objects.equals(comment, comment2)) {
                return commentDto2;
            } else if (Objects.equals(comment, comment3)) {
                return commentDto3;
            } else if (Objects.equals(comment, comment4)) {
                return commentDto4;
            }
            return null;
        });
        when(itemMapper.itemToItemInfoDto(any(Item.class))).thenAnswer((invocationOnMock) -> {
            Item item = invocationOnMock.getArgument(0);
            if (Objects.equals(item, item1)) {
                return itemInfoDto1;
            } else if (Objects.equals(item, item2)) {
                return itemInfoDto2;
            }
            return null;
        });

        List<ItemInfoDto> itemInfoDtos = itemService.getAllUserItems(user1.getId(), 1, 2);

        assertNotNull(itemInfoDtos);
        assertFalse(itemInfoDtos.isEmpty());
        assertEquals(2, itemInfoDtos.size());
        assertNotNull(itemInfoDtos.get(0));
        assertNotNull(itemInfoDtos.get(1));
        assertEquals(itemInfoDtoCheck1, itemInfoDtos.get(0));
        assertEquals(itemInfoDtoCheck2, itemInfoDtos.get(1));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1))
                .findAllByOwnerIdOrderById(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1)).findUserLastBookingsForEachItem(anyLong());
        verify(bookingMapper, times(2)).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(bookingRepository, times(1)).findUserLastBookingsForEachItem(anyLong());
        verify(commentRepository, times(1)).findAllCommentsLeftOnOwnerItemsWithId(anyLong());
        verify(commentMapper, times(4)).commentToCommentDto(any(Comment.class));
        verify(itemMapper, times(2)).itemToItemInfoDto(any(Item.class));
    }

    @Test
    @DisplayName("getAllUserItems_whenNoItems_thenEmptyListReturned")
    void getAllUserItems_whenNoItems_thenEmptyListReturned() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findAllByOwnerIdOrderById(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<ItemInfoDto> itemInfoDtos = itemService.getAllUserItems(user1.getId(), 1, 2);

        assertNotNull(itemInfoDtos);
        assertTrue(itemInfoDtos.isEmpty());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1))
                .findAllByOwnerIdOrderById(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findUserLastBookingsForEachItem(anyLong());
        verify(bookingMapper, never()).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(bookingRepository, never()).findUserLastBookingsForEachItem(anyLong());
        verify(commentRepository, never()).findAllCommentsLeftOnOwnerItemsWithId(anyLong());
        verify(commentMapper, never()).commentToCommentDto(any(Comment.class));
        verify(itemMapper, never()).itemToItemInfoDto(any(Item.class));
    }

    @Test
    @DisplayName("getAllUserItems_whenUserNotFound_thenNotFoundExceptionThrown")
    void getAllUserItems_whenUserNotFound_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.getAllUserItems(999L, 1, 2));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findAllByOwnerIdOrderById(anyLong(), any(Pageable.class));
        verify(bookingRepository, never()).findUserLastBookingsForEachItem(anyLong());
        verify(bookingMapper, never()).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(bookingRepository, never()).findUserLastBookingsForEachItem(anyLong());
        verify(commentRepository, never()).findAllCommentsLeftOnOwnerItemsWithId(anyLong());
        verify(commentMapper, never()).commentToCommentDto(any(Comment.class));
        verify(itemMapper, never()).itemToItemInfoDto(any(Item.class));
    }

    @Test
    @DisplayName("getItemById_whenOwnerOfItemInvoked_thenItemInfoDtoWithInformationAboutBookingsReturned")
    void getItemById_whenOwnerOfItemInvoked_thenItemInfoDtoWithInformationAboutBookingsReturned() {
        ItemInfoDto itemInfoDtoCheck = itemInfoDto1.toBuilder().lastBooking(itemInfoDtoBookingDto1)
                .nextBooking(itemInfoDtoBookingDto2).comments(List.of(commentDto1, commentDto2)).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(commentRepository.findAllCommentsByItemId(anyLong())).thenReturn(List.of(comment1, comment2));
        when(bookingRepository.findUserLastBooking(anyLong())).thenReturn(List.of(booking1));
        when(bookingRepository.findUserNextBooking(anyLong())).thenReturn(List.of(booking2));
        when(itemMapper.itemToItemInfoDto(any(Item.class))).thenReturn(itemInfoDto1);
        when(bookingMapper.bookingToItemInfoDtoBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return itemInfoDtoBookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return itemInfoDtoBookingDto2;
            }
            return null;
        });
        when(commentMapper.commentToCommentDto(any(Comment.class))).thenAnswer((invocationOnMock) -> {
            Comment comment = invocationOnMock.getArgument(0);
            if (Objects.equals(comment, comment1)) {
                return commentDto1;
            } else if (Objects.equals(comment, comment2)) {
                return commentDto2;
            }
            return null;
        });

        ItemInfoDto itemInfoDto = itemService.getItemById(item1.getId(), user1.getId());

        assertNotNull(itemInfoDto);
        assertEquals(itemInfoDtoCheck, itemInfoDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).findAllCommentsByItemId(anyLong());
        verify(bookingRepository, times(1)).findUserLastBooking(anyLong());
        verify(bookingRepository, times(1)).findUserNextBooking(anyLong());
        verify(itemMapper, times(1)).itemToItemInfoDto(any(Item.class));
        verify(bookingMapper, times(2)).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(commentMapper, times(2)).commentToCommentDto(any(Comment.class));
    }

    @Test
    @DisplayName("getItemById_whenSimpleUserInvoked_thenItemInfoDtoWithoutInformationAboutBookingsReturned")
    void getItemById_whenSimpleUserInvoked_thenItemInfoDtoWithoutInformationAboutBookingsReturned() {
        ItemInfoDto itemInfoDtoCheck = itemInfoDto1.toBuilder().comments(List.of(commentDto1, commentDto2)).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(commentRepository.findAllCommentsByItemId(anyLong())).thenReturn(List.of(comment1, comment2));
        when(itemMapper.itemToItemInfoDto(any(Item.class))).thenReturn(itemInfoDto1);
        when(commentMapper.commentToCommentDto(any(Comment.class))).thenAnswer((invocationOnMock) -> {
            Comment comment = invocationOnMock.getArgument(0);
            if (Objects.equals(comment, comment1)) {
                return commentDto1;
            } else if (Objects.equals(comment, comment2)) {
                return commentDto2;
            }
            return null;
        });

        ItemInfoDto itemInfoDto = itemService.getItemById(item1.getId(), user2.getId());

        assertNotNull(itemInfoDto);
        assertEquals(itemInfoDtoCheck, itemInfoDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).findAllCommentsByItemId(anyLong());
        verify(itemMapper, times(1)).itemToItemInfoDto(any(Item.class));
        verify(commentMapper, times(2)).commentToCommentDto(any(Comment.class));
        verify(bookingRepository, never()).findUserLastBooking(anyLong());
        verify(bookingRepository, never()).findUserNextBooking(anyLong());
        verify(bookingMapper, never()).bookingToItemInfoDtoBookingDto(any(Booking.class));
    }

    @Test
    @DisplayName("getItemById_whenOwnerOfItemInvokedAndNoComments_thenItemInfoDtoWithoutCommentsReturned")
    void getItemById_whenOwnerOfItemInvokedAndNoComments_thenItemInfoDtoWithoutCommentsReturned() {
        ItemInfoDto itemInfoDtoCheck = itemInfoDto1.toBuilder().lastBooking(itemInfoDtoBookingDto1)
                .nextBooking(itemInfoDtoBookingDto2).comments(Collections.emptyList()).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(commentRepository.findAllCommentsByItemId(anyLong())).thenReturn(Collections.emptyList());
        when(bookingRepository.findUserLastBooking(anyLong())).thenReturn(List.of(booking1));
        when(bookingRepository.findUserNextBooking(anyLong())).thenReturn(List.of(booking2));
        when(itemMapper.itemToItemInfoDto(any(Item.class))).thenReturn(itemInfoDto1);
        when(bookingMapper.bookingToItemInfoDtoBookingDto(any(Booking.class))).thenAnswer((invocationOnMock) -> {
            Booking booking = invocationOnMock.getArgument(0);
            if (Objects.equals(booking, booking1)) {
                return itemInfoDtoBookingDto1;
            } else if (Objects.equals(booking, booking2)) {
                return itemInfoDtoBookingDto2;
            }
            return null;
        });

        ItemInfoDto itemInfoDto = itemService.getItemById(item1.getId(), user1.getId());

        assertNotNull(itemInfoDto);
        assertEquals(itemInfoDtoCheck, itemInfoDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).findAllCommentsByItemId(anyLong());
        verify(bookingRepository, times(1)).findUserLastBooking(anyLong());
        verify(bookingRepository, times(1)).findUserNextBooking(anyLong());
        verify(itemMapper, times(1)).itemToItemInfoDto(any(Item.class));
        verify(bookingMapper, times(2)).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(commentMapper, never()).commentToCommentDto(any(Comment.class));
    }

    @Test
    @DisplayName("getItemById_whenOwnerOfItemInvokedAndNoNextBooking_thenItemInfoDtoWithoutNextBookingReturned")
    void getItemById_whenOwnerOfItemInvokedAndNoNextBooking_thenItemInfoDtoWithoutNextBookingReturned() {
        ItemInfoDto itemInfoDtoCheck = itemInfoDto1.toBuilder().lastBooking(itemInfoDtoBookingDto1)
                .comments(List.of(commentDto1, commentDto2)).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(commentRepository.findAllCommentsByItemId(anyLong())).thenReturn(List.of(comment1, comment2));
        when(bookingRepository.findUserLastBooking(anyLong())).thenReturn(List.of(booking1));
        when(bookingRepository.findUserNextBooking(anyLong())).thenReturn(Collections.emptyList());
        when(itemMapper.itemToItemInfoDto(any(Item.class))).thenReturn(itemInfoDto1);
        when(bookingMapper.bookingToItemInfoDtoBookingDto(any(Booking.class))).thenReturn(itemInfoDtoBookingDto1);
        when(commentMapper.commentToCommentDto(any(Comment.class))).thenAnswer((invocationOnMock) -> {
            Comment comment = invocationOnMock.getArgument(0);
            if (Objects.equals(comment, comment1)) {
                return commentDto1;
            } else if (Objects.equals(comment, comment2)) {
                return commentDto2;
            }
            return null;
        });

        ItemInfoDto itemInfoDto = itemService.getItemById(item1.getId(), user1.getId());

        assertNotNull(itemInfoDto);
        assertEquals(itemInfoDtoCheck, itemInfoDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).findAllCommentsByItemId(anyLong());
        verify(bookingRepository, times(1)).findUserLastBooking(anyLong());
        verify(bookingRepository, times(1)).findUserNextBooking(anyLong());
        verify(itemMapper, times(1)).itemToItemInfoDto(any(Item.class));
        verify(bookingMapper, times(1)).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(commentMapper, times(2)).commentToCommentDto(any(Comment.class));
    }

    @Test
    @DisplayName("getItemById_whenOwnerOfItemInvokedAndNoLastBooking_thenItemInfoDtoWithoutLastBookingReturned")
    void getItemById_whenOwnerOfItemInvokedAndNoLastBooking_thenItemInfoDtoWithoutLastBookingReturned() {
        ItemInfoDto itemInfoDtoCheck = itemInfoDto1.toBuilder().nextBooking(itemInfoDtoBookingDto2)
                .comments(List.of(commentDto1, commentDto2)).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(commentRepository.findAllCommentsByItemId(anyLong())).thenReturn(List.of(comment1, comment2));
        when(bookingRepository.findUserLastBooking(anyLong())).thenReturn(Collections.emptyList());
        when(bookingRepository.findUserNextBooking(anyLong())).thenReturn(List.of(booking2));
        when(itemMapper.itemToItemInfoDto(any(Item.class))).thenReturn(itemInfoDto1);
        when(bookingMapper.bookingToItemInfoDtoBookingDto(any(Booking.class))).thenReturn(itemInfoDtoBookingDto2);
        when(commentMapper.commentToCommentDto(any(Comment.class))).thenAnswer((invocationOnMock) -> {
            Comment comment = invocationOnMock.getArgument(0);
            if (Objects.equals(comment, comment1)) {
                return commentDto1;
            } else if (Objects.equals(comment, comment2)) {
                return commentDto2;
            }
            return null;
        });

        ItemInfoDto itemInfoDto = itemService.getItemById(item1.getId(), user1.getId());

        assertNotNull(itemInfoDto);
        assertEquals(itemInfoDtoCheck, itemInfoDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(commentRepository, times(1)).findAllCommentsByItemId(anyLong());
        verify(bookingRepository, times(1)).findUserLastBooking(anyLong());
        verify(bookingRepository, times(1)).findUserNextBooking(anyLong());
        verify(itemMapper, times(1)).itemToItemInfoDto(any(Item.class));
        verify(bookingMapper, times(1)).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(commentMapper, times(2)).commentToCommentDto(any(Comment.class));
    }

    @Test
    @DisplayName("getItemById_whenItemNotFound_thenNotFoundExceptionThrown")
    void getItemById_whenItemNotFound_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.getItemById(999L, user1.getId()));

        assertEquals("Item with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(commentRepository, never()).findAllCommentsByItemId(anyLong());
        verify(bookingRepository, never()).findUserLastBooking(anyLong());
        verify(bookingRepository, never()).findUserNextBooking(anyLong());
        verify(itemMapper, never()).itemToItemInfoDto(any(Item.class));
        verify(bookingMapper, never()).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(commentMapper, never()).commentToCommentDto(any(Comment.class));
    }

    @Test
    @DisplayName("getItemById_whenUserNotFound_thenNotFoundExceptionThrown")
    void getItemById_whenUserNotFound_thenNotFoundExceptionThrown() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.getItemById(item1.getId(), 999L));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(commentRepository, never()).findAllCommentsByItemId(anyLong());
        verify(bookingRepository, never()).findUserLastBooking(anyLong());
        verify(bookingRepository, never()).findUserNextBooking(anyLong());
        verify(itemMapper, never()).itemToItemInfoDto(any(Item.class));
        verify(bookingMapper, never()).bookingToItemInfoDtoBookingDto(any(Booking.class));
        verify(commentMapper, never()).commentToCommentDto(any(Comment.class));
    }

    @Test
    @DisplayName("updateItem_whenInvoked_thenUpdatedItemDtoReturned")
    void updateItem_whenInvoked_thenUpdatedItemDtoReturned() {
        Item itemToUpdate = Item.builder().name("updatedItem").description("updatedDescription")
                .available(Boolean.FALSE).build();
        Item updatedItem = item1.toBuilder().name("updatedItem").description("updatedDescription")
                .available(Boolean.FALSE).build();
        ItemDto updatedItemDto = itemDto1.toBuilder().name("updatedItem").description("updatedDescription")
                .available(Boolean.FALSE).build();
        ItemDto updatedItemDtoCheck = updatedItemDto.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(itemMapper.updateItem(any(Item.class), any(Item.class))).thenReturn(updatedItem);
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.itemToItemDto(any(Item.class))).thenReturn(updatedItemDto);

        ItemDto itemDto = itemService.updateItem(item1.getId(), itemToUpdate, user1.getId());

        assertNotNull(itemDto);
        assertEquals(updatedItemDtoCheck, itemDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemMapper, times(1)).updateItem(any(Item.class), any(Item.class));
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemMapper, times(1)).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("updateItem_whenNothingToUpdate_thenUpdatedItemDtoReturned")
    void updateItem_whenNothingToUpdate_thenUpdatedItemDtoReturned() {
        Item itemToUpdate = Item.builder().build();
        ItemDto updatedItemDto = itemDto1.toBuilder().build();
        ItemDto updatedItemDtoCheck = updatedItemDto.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(itemMapper.updateItem(any(Item.class), any(Item.class))).thenReturn(item1);
        when(itemRepository.save(any(Item.class))).thenReturn(item1);
        when(itemMapper.itemToItemDto(any(Item.class))).thenReturn(updatedItemDto);

        ItemDto itemDto = itemService.updateItem(item1.getId(), itemToUpdate, user1.getId());

        assertNotNull(itemDto);
        assertEquals(updatedItemDtoCheck, itemDto);
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemMapper, times(1)).updateItem(any(Item.class), any(Item.class));
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(itemMapper, times(1)).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("updateItem_whenItemNotFound_thenNotFoundExceptionThrown")
    void updateItem_whenItemNotFound_thenNotFoundExceptionThrown() {
        Item itemToUpdate = Item.builder().name("updatedItem").description("updatedDescription")
                .available(Boolean.FALSE).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.updateItem(999L, itemToUpdate, user1.getId()));

        assertEquals("Item with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemMapper, never()).updateItem(any(Item.class), any(Item.class));
        verify(itemRepository, never()).save(any(Item.class));
        verify(itemMapper, never()).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("updateItem_whenUserNotFound_thenNotFoundExceptionThrown")
    void updateItem_whenUserNotFound_thenNotFoundExceptionThrown() {
        Item itemToUpdate = Item.builder().name("updatedItem").description("updatedDescription")
                .available(Boolean.FALSE).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.updateItem(item1.getId(), itemToUpdate, 999L));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(itemMapper, never()).updateItem(any(Item.class), any(Item.class));
        verify(itemRepository, never()).save(any(Item.class));
        verify(itemMapper, never()).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("searchItems_whenInvoked_thenListOfItemsReturned")
    void searchItems_whenInvoked_thenListOfItemsReturned() {
        Item item2 = Item.builder().id(2L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).owner(user1).build();
        ItemDto itemDto2 = ItemDto.builder().id(1L).name("item2").description("itemDescription2")
                .available(Boolean.TRUE).build();
        ItemDto itemDtoCheck1 = itemDto1.toBuilder().build();
        ItemDto itemDtoCheck2 = itemDto2.toBuilder().build();
        when(itemRepository.searchItems(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item1, item2)));
        when(itemMapper.itemToItemDto(any(Item.class))).thenAnswer((invocationOnMock) -> {
            Item item = invocationOnMock.getArgument(0);
            if (Objects.equals(item, item1)) {
                return itemDto1;
            } else if (Objects.equals(item, item2)) {
                return itemDto2;
            }
            return null;
        });

        List<ItemDto> itemDtos = itemService.searchItems("item", 1, 2);

        assertNotNull(itemDtos);
        assertEquals(2, itemDtos.size());
        assertEquals(itemDtoCheck1, itemDtos.get(0));
        assertEquals(itemDtoCheck2, itemDtos.get(1));
        verify(itemRepository, times(1)).searchItems(anyString(), any(Pageable.class));
        verify(itemMapper, times(2)).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("searchItems_whenNoItems_thenEmptyListReturned")
    void searchItems_whenNoItems_thenEmptyListReturned() {
        when(itemRepository.searchItems(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        List<ItemDto> itemDtos = itemService.searchItems("item", 1, 2);

        assertNotNull(itemDtos);
        assertTrue(itemDtos.isEmpty());
        verify(itemRepository, times(1)).searchItems(anyString(), any(Pageable.class));
        verify(itemMapper, never()).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("searchItems_whenEmptyText_thenEmptyListReturned")
    void searchItems_whenEmptyText_thenEmptyListReturned() {
        List<ItemDto> itemDtos = itemService.searchItems("", 1, 2);

        assertNotNull(itemDtos);
        assertTrue(itemDtos.isEmpty());
        verify(itemRepository, never()).searchItems(anyString(), any(Pageable.class));
        verify(itemMapper, never()).itemToItemDto(any(Item.class));
    }

    @Test
    @DisplayName("createComment_whenInvoke_thenCommentDtoWithIdReturned")
    void createComment_whenInvoke_thenCommentDtoWithIdReturned() {
        Comment commentToCreate = Comment.builder().text("comment1").item(item1).author(user2).build();
        CommentDto commentDtoCheck = commentDto1.toBuilder().build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(bookingRepository.findAllFinishedBookingsByUserAndItem(anyLong(), anyLong()))
                .thenReturn(List.of(booking1));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment1);
        when(commentMapper.commentToCommentDto(any(Comment.class))).thenReturn(commentDto1);

        CommentDto commentDto = itemService.createComment(commentToCreate, user2.getId(), item1.getId());

        assertNotNull(commentDto);
        assertEquals(commentDtoCheck.getId(), commentDto.getId());
        assertEquals(commentDtoCheck.getText(), commentDto.getText());
        assertEquals(commentDtoCheck.getAuthorName(), commentDto.getAuthorName());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllFinishedBookingsByUserAndItem(anyLong(), anyLong());
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(commentMapper, times(1)).commentToCommentDto(any(Comment.class));
    }

    @Test
    @DisplayName("createComment_whenNoFinishedBookings_thenAccessToAddCommentDeniedExceptionThrown")
    void createComment_whenNoFinishedBookings_thenAccessToAddCommentDeniedExceptionThrown() {
        Comment commentToCreate = Comment.builder().text("comment1").item(item1).author(user2).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(bookingRepository.findAllFinishedBookingsByUserAndItem(anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        AccessToAddCommentDeniedException exception = assertThrows(AccessToAddCommentDeniedException.class, () ->
                itemService.createComment(commentToCreate, user2.getId(), item1.getId()));

        assertEquals("User with id = 2 haven't done any bookings yet", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1))
                .findAllFinishedBookingsByUserAndItem(anyLong(), anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
        verify(commentMapper, never()).commentToCommentDto(any(Comment.class));
    }

    @Test
    @DisplayName("createComment_whenItemNotFound_thenNotFoundExceptionThrown")
    void createComment_whenItemNotFound_thenNotFoundExceptionThrown() {
        Comment commentToCreate = Comment.builder().text("comment1").item(item1).author(user2).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.createComment(commentToCreate, user2.getId(), 999L));

        assertEquals("Item with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findAllFinishedBookingsByUserAndItem(anyLong(), anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
        verify(commentMapper, never()).commentToCommentDto(any(Comment.class));
    }

    @Test
    @DisplayName("createComment_whenUserNotFound_thenNotFoundExceptionThrown")
    void createComment_whenUserNotFound_thenNotFoundExceptionThrown() {
        Comment commentToCreate = Comment.builder().text("comment1").item(item1).author(user2).build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.createComment(commentToCreate, 999L, item1.getId()));

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).findAllFinishedBookingsByUserAndItem(anyLong(), anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
        verify(commentMapper, never()).commentToCommentDto(any(Comment.class));
    }
}
