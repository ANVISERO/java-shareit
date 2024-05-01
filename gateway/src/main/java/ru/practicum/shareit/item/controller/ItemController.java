package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<Object> createItem(@Validated({OnCreate.class, Default.class}) @RequestBody ItemDto itemDto,
                                             @RequestHeader(value = USER_HEADER) @Positive @NotNull Long userId) {
        log.debug("POST request received to create a new item {} from user with id = {}", itemDto, userId);
        return itemClient.createItem(itemDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserItems(@RequestHeader(value = USER_HEADER) @Positive @NotNull Long userId,
                                                  @RequestParam(name = "from", defaultValue = "0")
                                                  @PositiveOrZero int from,
                                                  @RequestParam(name = "size", defaultValue = "10")
                                                  @Positive int size) {
        log.debug("GET request received to get all items of the user with id = {}", userId);
        return itemClient.getAllUserItems(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable(name = "itemId") @Positive @NotNull Long itemId,
                                              @RequestHeader(value = USER_HEADER) @Positive @NotNull Long userId) {
        log.debug("GET request received to get item by id = {} from user with id = {}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable(name = "itemId") @Positive @NotNull Long itemId,
                                             @Valid @RequestBody ItemDto itemDto,
                                             @RequestHeader(value = USER_HEADER) @Positive @NotNull Long ownerId) {
        log.debug("PATCH request received to update item by id = {} " +
                "from user with id = {}", itemId, ownerId);
        return itemClient.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam("text") String text,
                                              @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        log.debug("GET request received to search items by text = {}", text);
        return itemClient.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@Validated({OnCreate.class, Default.class}) @RequestBody CommentDto commentDto,
                                                @PathVariable(name = "itemId") @Positive Long itemId,
                                                @RequestHeader(value = USER_HEADER) @Positive @NotNull Long authorId) {
        log.debug("POST request received to create a new comment {} from user with id = {} and item with id = {}",
                commentDto, authorId, itemId);
        return itemClient.createComment(commentDto, authorId, itemId);
    }
}
