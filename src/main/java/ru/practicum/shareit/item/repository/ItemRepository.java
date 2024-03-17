package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerIdOrderById(Long userId);

    @Query("SELECT i FROM Item AS i WHERE i.available = true " +
            "AND (LOWER(i.name) like LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) like LOWER(CONCAT('%', :text, '%')))")
    List<Item> searchItems(String text);
}
