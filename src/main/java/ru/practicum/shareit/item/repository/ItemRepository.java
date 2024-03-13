package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item createItem(Item item);

    List<Item> getAllItems(Integer userId);

    Item getItemById(Integer itemId);

    Item updateItem(Item item);

    List<Item> searchItems(String text);
}
