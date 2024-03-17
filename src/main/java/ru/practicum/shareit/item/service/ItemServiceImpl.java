package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.util.mapper.BookingMapper;
import ru.practicum.shareit.exception.AccessToAddCommentDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.util.mapper.CommentMapper;
import ru.practicum.shareit.item.util.mapper.ItemMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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

    @Override
    @Transactional
    public ItemDto createItem(Item item, Long userId) {
        User owner = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new NotFoundException(
                    String.format("User with id %d not found", userId));
        });
        log.debug("User with id = {} found", userId);
        item.setOwner(owner);
        item.setAvailable(Boolean.TRUE);
        return ItemMapper.itemToItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public List<ItemDto> getAllUserItems(Long userId) {
        User owner = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id {} not found", userId);
            return new NotFoundException(String.format("User with id %d not found", userId));
        });
        log.debug("User with id = {} found", userId);
        Map<Long, BookingDto> lastBookingDtoForItem = bookingRepository.findUserLastBookingsForEachItem(userId)
                .stream()
                .collect(Collectors.toMap(booking -> booking.getItem().getId(),
                        BookingMapper::bookingToBookingDtoWithoutUserAndItem,
                        (present, another) -> present));
        Map<Long, BookingDto> nextBookingDtoForItem = bookingRepository.findUserNextBookingsForEachItem(userId)
                .stream()
                .collect(Collectors.toMap(booking -> booking.getItem().getId(),
                        BookingMapper::bookingToBookingDtoWithoutUserAndItem,
                        (present, another) -> present));
        List<Comment> comments = commentRepository.findAllCommentsByAuthorId(userId);
        return itemRepository.findAllByOwnerIdOrderById(userId).stream()
                .map(item -> {
                    ItemDto itemDto = ItemMapper.itemToItemDto(item);
                    itemDto.setLastBooking(lastBookingDtoForItem.get(item.getId()));
                    itemDto.setNextBooking(nextBookingDtoForItem.get(item.getId()));
                    itemDto.setComments(comments.stream().map(CommentMapper::commentToCommentDto)
                            .collect(Collectors.toList()));
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long itemId, Long ownerId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found", itemId);
            return new NotFoundException(String.format("Item with id %d not found", itemId));
        });
        log.debug("Item with id {} found", itemId);
        List<Comment> comments = commentRepository.findAllCommentsByItemId(itemId);
        if (comments.isEmpty()) {
            log.warn("Comments by item with id {} not found", itemId);
        } else {
            log.debug("Comments by item with id = {} found", itemId);
        }
        if (!Objects.equals(item.getOwner().getId(), ownerId)) {
            ItemDto itemDto = ItemMapper.itemToItemDto(item);
            itemDto.setComments(comments.isEmpty() ? Collections.emptyList() : comments.stream()
                    .map(CommentMapper::commentToCommentDto)
                    .collect(Collectors.toList()));
            return itemDto;
        }
        List<Booking> lastBooking = bookingRepository.findUserLastBooking(itemId);
        List<Booking> nextBooking = bookingRepository.findUserNextBooking(itemId);
        log.debug("lastBooking: {} nextBooking: {}", lastBooking, nextBooking);
        ItemDto itemDto = ItemMapper.itemToItemDto(item);
        itemDto.setLastBooking(lastBooking.isEmpty() ?
                null : BookingMapper.bookingToBookingDtoWithoutUserAndItem(lastBooking.get(0)));
        itemDto.setNextBooking(nextBooking.isEmpty() ?
                null : BookingMapper.bookingToBookingDtoWithoutUserAndItem(nextBooking.get(0)));
        itemDto.setComments(comments.isEmpty() ? Collections.emptyList() : comments.stream()
                .map(CommentMapper::commentToCommentDto)
                .collect(Collectors.toList()));
        return itemDto;
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, Item item, Long ownerId) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> {
            log.warn("User with id {} not found", ownerId);
            return new NotFoundException(
                    String.format("User with id %d not found", ownerId));
        });
        log.debug("User with id = {} found", ownerId);
        Item existingItem = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found", itemId);
            return new NotFoundException(String.format("Item with id %d not found", itemId));
        });
        log.debug("Item with id = {} found", itemId);
        item.setId(itemId);
        item.setOwner(owner);
        return ItemMapper.itemToItemDto(itemRepository.save(ItemMapper.updateItem(existingItem, item)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> searchItems(String text) {
        if (text.isEmpty()) {
            log.debug("Text for search is empty");
            return Collections.emptyList();
        }
        log.debug("Text for search: {}", text);
        return itemRepository.searchItems(text).stream()
                .map(ItemMapper::itemToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(Comment comment, Long authorId, Long itemId) {
        User author = userRepository.findById(authorId).orElseThrow(() -> {
            log.warn("User with id {} not found", authorId);
            return new NotFoundException(
                    String.format("User with id %d not found", authorId));
        });
        log.debug("User with id = {} found", authorId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Item with id {} not found", itemId);
            return new NotFoundException(
                    String.format("Item with id %d not found", itemId));
        });
        log.debug("Item with id = {} found", itemId);
        if (bookingRepository.findAllFinishedBookingsByUserAndItem(authorId, itemId).isEmpty()) {
            log.warn("User with id = {} haven't done any bookings yet", itemId);
            throw new AccessToAddCommentDeniedException(
                    String.format("User with id = %d haven't done any bookings yet", itemId));
        }
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.commentToCommentDto(commentRepository.save(comment));
    }
}
