package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Item item, Long userId);

    List<ItemDto> getAllUserItems(Long userId);

    ItemDto getItemById(Long itemId, Long ownerId);

    ItemDto updateItem(Long itemId, Item item, Long ownerId);

    List<ItemDto> searchItems(String text);

    CommentDto createComment(Comment comment, Long authorId, Long itemId);
}
