package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryItemRepository implements ItemRepository {
    private static final Map<Integer, List<Item>> usersItems = new HashMap<>();
    private static final Map<Integer, Item> items = new HashMap<>();
    private static int itemId = 0;

    @Override
    public Item createItem(Item item) {
        item.setId(generateItemId());
        List<Item> currentItems = usersItems.getOrDefault(item.getOwnerId(), new ArrayList<>());
        currentItems.add(item);
        usersItems.put(item.getOwnerId(), currentItems);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> getAllItems(Integer userId) {
        return usersItems.get(userId);
    }

    @Override
    public Item getItemById(Integer itemId) {
        checkItemExists(itemId);
        log.debug("Вещь с идентификатором id = {} найдена", itemId);
        return items.get(itemId);
    }

    @Override
    public Item updateItem(Item item) {
        checkUsersItemExists(item.getId(), item.getOwnerId());
        log.debug("Вещь с идентификатором id = {} найдена", item.getId());
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
            return new ArrayList<>();
        }
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter((item) -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase())).collect(Collectors.toList());
    }

    private static Integer generateItemId() {
        return ++itemId;
    }

    private static void checkItemExists(Integer itemId) {
        if (!items.containsKey(itemId)) {
            log.warn("Вещь с идентификатором id = {} не найдена", itemId);
            throw new NotFoundException(String.format("Вещь с идентификатором id = %s не найдена", itemId));
        }
    }

    private static void checkUsersItemExists(Integer itemId, Integer ownerId) {
        List<Item> userItems = usersItems.get(ownerId);
        if (userItems == null || userItems.stream().filter((Item item) -> item.getId().equals(itemId)).findAny().isEmpty()) {
            log.warn("Вещь с идентификатором id = {} не найдена у пользователя с идентификатором id = {}",
                    itemId, ownerId);
            throw new NotFoundException(String.format("Вещь с идентификатором id = %s не найдена у пользователя " +
                    "с идентификатором id = %s", itemId, ownerId));
        }
    }
}
