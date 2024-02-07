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

/**
 * TODO Sprint add-controllers.
 */

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
        log.debug("Получен Post запрос для создания новой вещи {} пользователя с идентификатором id = {}",
                itemDto, userId);
        Item item = ItemMapper.itemDtoToItem(itemDto);
        log.debug("Тело сообщения преобразовано в объект {}", item);
        return itemService.createItem(item, userId);
    }

    @GetMapping
    public List<Item> getAllItems(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        log.debug("Получен Get запрос для получения всех вещей пользователя с идентификатором id = {}", userId);
        return itemService.getAllItems(userId);
    }

    @GetMapping("/{itemId}")
    public Item getItemById(@PathVariable(name = "itemId") Integer itemId) {
        log.debug("Получен Get запрос для получения вещи по её уникальному идентификатору id = {}", itemId);
        return itemService.getItemById(itemId);
    }

    @PatchMapping("/{itemId}")
    public Item updateItem(@PathVariable(name = "itemId") Integer itemId, @RequestBody ItemDto itemDto,
                           @RequestHeader(value = "X-Sharer-User-Id") Integer ownerId) {
        log.debug("Получен Patch запрос для обновления вещи по её уникальному идентификатору id = {} " +
                "от пользователя с идентификатором id = {}", itemId, ownerId);
        Item item = ItemMapper.itemDtoToItem(itemDto);
        log.debug("Тело сообщения преобразовано в объект {}", item);
        return itemService.updateItem(itemId, item, ownerId);
    }

    @GetMapping("/search")
    public List<Item> searchItem(@RequestParam("text") String text) {
        log.debug("Получен Get запрос для поиска вещи по ключевому слову text = {}", text);
        return itemService.searchItems(text);
    }
}
