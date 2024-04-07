package ru.practicum.shareit.item.util.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInfoDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestInfoDto;

@Component
public class ItemMapper {

    public Item itemDtoToItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public ItemDto itemToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getItemRequest() == null ? null : item.getItemRequest().getId())
                .build();
    }

    public ItemInfoDto itemToItemInfoDto(Item item) {
        return ItemInfoDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public Item updateItem(Item existingItem, Item item) {
        return Item.builder()
                .id(existingItem.getId())
                .name(item.getName() != null ? item.getName() : existingItem.getName())
                .description(item.getDescription() != null ? item.getDescription() : existingItem.getDescription())
                .available(item.getAvailable() != null ? item.getAvailable() : existingItem.getAvailable())
                .owner(existingItem.getOwner())
                .build();
    }

    public ItemRequestInfoDto.ItemDto itemToItemRequestInfoDtoItemDto(Item item) {
        return ItemRequestInfoDto.ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getItemRequest() == null ? null : item.getItemRequest().getId())
                .build();
    }
}
