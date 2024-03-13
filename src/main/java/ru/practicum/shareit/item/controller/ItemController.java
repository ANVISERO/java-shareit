package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.OnCreate;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public Item createItem(@Validated({OnCreate.class}) @RequestBody ItemDto itemDto,
                           @RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("POST request received to create a new item {} of a user with id = {}",
                itemDto, userId);
        Item item = ItemMapper.itemDtoToItem(itemDto);
        log.debug("The message body converted to an object {}", item);
        return itemService.createItem(item, userId);
    }

    @GetMapping
    public List<Item> getAllItems(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("GET request received to get all items of the user with id = {}", userId);
        return itemService.getAllItems(userId);
    }

    @GetMapping("/{itemId}")
    public Item getItemById(@PathVariable(name = "itemId") Integer itemId) {
        log.debug("GET request received to get item by id = {}", itemId);
        return itemService.getItemById(itemId);
    }

    @PatchMapping("/{itemId}")
    public Item updateItem(@PathVariable(name = "itemId") Integer itemId, @RequestBody ItemDto itemDto,
                           @RequestHeader(value = "X-Sharer-User-Id") Integer ownerId) {
        log.debug("PATCH request received to update item by id = {} " +
                "from user with id = {}", itemId, ownerId);
        Item item = ItemMapper.itemDtoToItem(itemDto);
        log.debug("The message body converted to an object {}", item);
        return itemService.updateItem(itemId, item, ownerId);
    }

    @GetMapping("/search")
    public List<Item> searchItem(@RequestParam("text") String text) {
        log.debug("GET request received to search items by text = {}", text);
        return itemService.searchItems(text);
    }
}
