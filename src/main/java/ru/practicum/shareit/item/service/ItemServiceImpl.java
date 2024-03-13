package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item createItem(Item item, Integer userId) {
        userRepository.checkUserExists(userId);
        log.debug("User with id = {} not found", userId);
        item.setOwnerId(userId);
        return itemRepository.createItem(item);
    }

    @Override
    public List<Item> getAllItems(Integer userId) {
        return itemRepository.getAllItems(userId);
    }

    @Override
    public Item getItemById(Integer itemId) {
        return itemRepository.getItemById(itemId);
    }

    @Override
    public Item updateItem(Integer itemId, Item item, Integer ownerId) {
        userRepository.checkUserExists(ownerId);
        log.debug("User with id = {} not found", ownerId);
        item.setId(itemId);
        item.setOwnerId(ownerId);
        return itemRepository.updateItem(item);
    }

    @Override
    public List<Item> searchItems(String text) {
        return itemRepository.searchItems(text);
    }
}
