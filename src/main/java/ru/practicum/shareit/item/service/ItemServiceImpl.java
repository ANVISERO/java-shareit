package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
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
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.ShareItPageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto createItem(Item item, Long userId, Long requestId) {
        User owner = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new NotFoundException(String.format("User with id %d not found", userId));
        });
        if (requestId != null) {
            item.setItemRequest(itemRequestRepository.findById(requestId).orElseThrow(() -> {
                log.warn("Item request with id {} not found", requestId);
                return new NotFoundException(String.format("Item request with id %d not found", requestId));
            }));
            log.debug("Item request request with id = {} found", requestId);
        }
        item.setOwner(owner);
        item.setAvailable(Boolean.TRUE);
        return itemMapper.itemToItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public List<ItemInfoDto> getAllUserItems(Long userId, int from, int size) {
        userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new NotFoundException(String.format("User with id %d not found", userId));
        });
        log.debug("User with id = {} found", userId);
        ShareItPageRequest pageRequest = new ShareItPageRequest(from, size);
        Page<Item> itemsPage = itemRepository.findAllByOwnerIdOrderById(userId, pageRequest);
        if (itemsPage.isEmpty()) {
            log.warn("Items not found");
            return Collections.emptyList();
        }
        Map<Long, ItemInfoDto.BookingDto> lastBookingDtoForItem = bookingRepository
                .findUserLastBookingsForEachItem(userId)
                .stream()
                .collect(Collectors.toMap(booking -> booking.getItem().getId(),
                        bookingMapper::bookingToItemInfoDtoBookingDto,
                        (present, another) -> present));
        Map<Long, ItemInfoDto.BookingDto> nextBookingDtoForItem = bookingRepository
                .findUserNextBookingsForEachItem(userId)
                .stream()
                .collect(Collectors.toMap(booking -> booking.getItem().getId(),
                        bookingMapper::bookingToItemInfoDtoBookingDto,
                        (present, another) -> present));
        Map<Long, List<Comment>> comments = commentRepository
                .findAllCommentsLeftOnOwnerItemsWithId(userId)
                .stream()
                .collect(Collectors.groupingBy((comment) -> comment.getItem().getId()));
        return itemsPage.getContent().stream()
                .map(item -> {
                    ItemInfoDto itemInfoDto = itemMapper.itemToItemInfoDto(item);
                    itemInfoDto.setLastBooking(lastBookingDtoForItem.get(item.getId()));
                    itemInfoDto.setNextBooking(nextBookingDtoForItem.get(item.getId()));
                    itemInfoDto.setComments(comments.get(itemInfoDto.getId()) == null
                            ? Collections.emptyList()
                            : comments.get(itemInfoDto.getId()).stream()
                            .map(commentMapper::commentToCommentDto)
                            .collect(Collectors.toList()));
                    return itemInfoDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemInfoDto getItemById(Long itemId, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new NotFoundException(String.format("User with id %d not found", userId));
        });
        log.debug("User with id = {} found", userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found", itemId);
            return new NotFoundException(String.format("Item with id %d not found", itemId));
        });
        log.debug("Item with id {} found", itemId);
        List<Comment> comments = commentRepository.findAllCommentsByItemId(itemId);
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            ItemInfoDto itemInfoDto = itemMapper.itemToItemInfoDto(item);
            itemInfoDto.setComments(comments.stream().map(commentMapper::commentToCommentDto)
                    .collect(Collectors.toList()));
            return itemInfoDto;
        }
        List<Booking> lastBooking = bookingRepository.findUserLastBooking(itemId);
        List<Booking> nextBooking = bookingRepository.findUserNextBooking(itemId);
        ItemInfoDto itemInfoDto = itemMapper.itemToItemInfoDto(item);
        itemInfoDto.setLastBooking(lastBooking.isEmpty() ?
                null : bookingMapper.bookingToItemInfoDtoBookingDto(lastBooking.get(0)));
        itemInfoDto.setNextBooking(nextBooking.isEmpty() ?
                null : bookingMapper.bookingToItemInfoDtoBookingDto(nextBooking.get(0)));
        itemInfoDto.setComments(comments.isEmpty() ? Collections.emptyList() : comments.stream()
                .map(commentMapper::commentToCommentDto)
                .collect(Collectors.toList()));
        return itemInfoDto;
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, Item item, Long ownerId) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> {
            log.warn("User with id {} not found", ownerId);
            return new NotFoundException(String.format("User with id %d not found", ownerId));
        });
        log.debug("User with id = {} found", ownerId);
        Item existingItem = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found", itemId);
            return new NotFoundException(String.format("Item with id %d not found", itemId));
        });
        log.debug("Item with id = {} found", itemId);
        item.setId(itemId);
        item.setOwner(owner);
        return itemMapper.itemToItemDto(itemRepository.save(itemMapper.updateItem(existingItem, item)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItems(String text, int from, int size) {
        if (text.isEmpty()) {
            log.debug("Text for search is empty");
            return Collections.emptyList();
        }
        log.debug("Text for search: {}", text);
        ShareItPageRequest pageRequest = new ShareItPageRequest(from, size);
        Page<Item> itemsPage = itemRepository.searchItems(text, pageRequest);
        if (itemsPage.isEmpty()) {
            log.warn("Items not found");
            return Collections.emptyList();
        }
        return itemsPage.getContent().stream()
                .map(itemMapper::itemToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(Comment comment, Long authorId, Long itemId) {
        User author = userRepository.findById(authorId).orElseThrow(() -> {
            log.warn("User with id {} not found", authorId);
            return new NotFoundException(String.format("User with id %d not found", authorId));
        });
        log.debug("User with id = {} found", authorId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found", itemId);
            return new NotFoundException(String.format("Item with id %d not found", itemId));
        });
        log.debug("Item with id = {} found", itemId);
        if (bookingRepository.findAllFinishedBookingsByUserAndItem(authorId, itemId).isEmpty()) {
            log.warn("User with id = {} haven't done any bookings yet", authorId);
            throw new AccessToAddCommentDeniedException(
                    String.format("User with id = %d haven't done any bookings yet", authorId));
        }
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return commentMapper.commentToCommentDto(commentRepository.save(comment));
    }
}
