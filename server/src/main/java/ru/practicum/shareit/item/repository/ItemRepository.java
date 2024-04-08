package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findAllByOwnerIdOrderById(Long userId, Pageable pageable);

    @Query("SELECT i FROM Item AS i WHERE i.available = true " +
            "AND (LOWER(i.name) like LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) like LOWER(CONCAT('%', :text, '%')))")
    Page<Item> searchItems(String text, Pageable pageable);

    List<Item> findAllByItemRequestIn(@Param("requests") List<ItemRequest> requests);

    List<Item> findAllByItemRequest(@Param("requests") ItemRequest itemRequest);
}
