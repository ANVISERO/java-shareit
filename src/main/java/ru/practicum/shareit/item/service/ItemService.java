package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item createItem(Item item, Integer userId);

    List<Item> getAllItems(Integer userId);

    Item getItemById(Integer itemId);

    Item updateItem(Integer itemId, Item item, Integer ownerId);

    List<Item> searchItems(String text);
}
