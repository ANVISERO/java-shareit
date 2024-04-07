package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.util.mapper.CommentMapper;
import ru.practicum.shareit.item.util.mapper.ItemMapper;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final String userHeader = "X-Sharer-User-Id";


    @PostMapping
    public ItemDto createItem(@Validated({OnCreate.class, Default.class}) @RequestBody ItemDto itemDto,
                              @RequestHeader(value = userHeader) @Positive @NotNull Long userId) {
        log.debug("POST request received to create a new item {} from user with id = {}", itemDto, userId);
        Item item = itemMapper.itemDtoToItem(itemDto);
        log.debug("The message body converted to an object {}", item);
        return itemService.createItem(item, userId, itemDto.getRequestId());
    }

    @GetMapping
    public List<ItemInfoDto> getAllUserItems(@RequestHeader(value = userHeader) @Positive @NotNull Long userId,
                                             @RequestParam(name = "from", defaultValue = "0")
                                             @PositiveOrZero int from,
                                             @RequestParam(name = "size", defaultValue = "10")
                                             @Positive int size) {
        log.debug("GET request received to get all items of the user with id = {}", userId);
        return itemService.getAllUserItems(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ItemInfoDto getItemById(@PathVariable(name = "itemId") @Positive @NotNull Long itemId,
                                   @RequestHeader(value = userHeader) @Positive @NotNull Long userId) {
        log.debug("GET request received to get item by id = {} from user with id = {}", itemId, userId);
        return itemService.getItemById(itemId, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable(name = "itemId") @Positive @NotNull Long itemId,
                              @Valid @RequestBody ItemDto itemDto,
                              @RequestHeader(value = userHeader) @Positive @NotNull Long ownerId) {
        log.debug("PATCH request received to update item by id = {} " +
                "from user with id = {}", itemId, ownerId);
        Item item = itemMapper.itemDtoToItem(itemDto);
        log.debug("The message body converted to an object {}", item);
        return itemService.updateItem(itemId, item, ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam("text") String text,
                                    @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                    @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.debug("GET request received to search items by text = {}", text);
        return itemService.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@Validated({OnCreate.class, Default.class}) @RequestBody CommentDto commentDto,
                                    @PathVariable(name = "itemId") @Positive Long itemId,
                                    @RequestHeader(value = userHeader) @Positive @NotNull Long authorId) {
        log.debug("POST request received to create a new comment {} from user with id = {} and item with id = {}",
                commentDto, authorId, itemId);
        Comment comment = commentMapper.commentDtoToComment(commentDto);
        log.debug("The message body converted to an object {}", comment);
        return itemService.createComment(comment, authorId, itemId);
    }
}
