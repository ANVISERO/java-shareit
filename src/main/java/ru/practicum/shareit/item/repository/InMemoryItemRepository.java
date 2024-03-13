package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Integer, List<Item>> usersItems = new HashMap<>();
    private final Map<Integer, Item> items = new HashMap<>();
    private int itemId = 0;

    @Override
    public Item createItem(Item item) {
        item.setId(generateItemId());
        usersItems.computeIfAbsent(item.getOwnerId(), key -> new ArrayList<>()).add(item);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> getAllItems(Integer userId) {
        return usersItems.getOrDefault(userId, Collections.emptyList());
    }

    @Override
    public Item getItemById(Integer itemId) {
        return items.computeIfAbsent(itemId, key -> {
            log.warn("Item with id = {} not found", itemId);
            throw new NotFoundException(String.format("Вещь с идентификатором id = %s не найдена", itemId));
        });
    }

    @Override
    public Item updateItem(Item item) {
        checkUsersItemExists(item.getId(), item.getOwnerId());
        log.debug("Item with id = {} not found", item.getId());
        List<Item> currentItems = usersItems.get(item.getOwnerId());
        Item updatedItem = currentItems.stream()
                .filter((Item item1) -> item1.getId().equals(item.getId()))
                .findAny().get();
        if (item.getName() != null) {
            updatedItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updatedItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }
        currentItems.removeIf((Item item1) -> item1.getId().equals(item.getId()));
        currentItems.add(updatedItem);
        items.remove(item.getId());
        items.put(updatedItem.getId(), updatedItem);
        return updatedItem;
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        String lowerCaseText = text.trim().toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter((item) -> item.getName().toLowerCase().contains(lowerCaseText)
                        || item.getDescription().toLowerCase().contains(lowerCaseText))
                .collect(Collectors.toList());
    }

    private Integer generateItemId() {
        return ++itemId;
    }

    private void checkUsersItemExists(Integer itemId, Integer ownerId) {
        List<Item> userItems = usersItems.get(ownerId);
        if (userItems == null ||
                userItems.stream()
                        .filter((Item item) -> item.getId().equals(itemId))
                        .findAny()
                        .isEmpty()) {
            log.warn("Item with id = {} not found in the user with id = {}", itemId, ownerId);
            throw new NotFoundException(
                    String.format("Item with id = %s not found in the user with id = %s", itemId, ownerId));
        }
    }
}
