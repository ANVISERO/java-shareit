package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Item item, Long userId, Long requestId);

    List<ItemInfoDto> getAllUserItems(Long userId, int from, int size);

    ItemInfoDto getItemById(Long itemId, Long userId);

    ItemDto updateItem(Long itemId, Item item, Long ownerId);

    List<ItemDto> searchItems(String text, int from, int size);

    CommentDto createComment(Comment comment, Long authorId, Long itemId);
}
