package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.ShareItPageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    User user1;
    Item item1;
    Item item2;

    @BeforeEach
    void setUp() {
        user1 = User.builder().name("user1").email("user1@post.com").build();
        userRepository.save(user1);
        item1 = Item.builder().name("item1").description("itemDescription1")
                .available(Boolean.TRUE).owner(user1).build();
        item2 = Item.builder().name("item2").description("itemDescription2")
                .available(Boolean.TRUE).owner(user1).build();
        itemRepository.save(item1);
        itemRepository.save(item2);
    }

    @AfterEach
    void clearDatabase() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("searchItems_whenInvoked_thenPageOfItemsReturned")
    void searchItems_whenInvoked_thenPageOfItemsReturned() {
        ShareItPageRequest pageRequest = new ShareItPageRequest(0, 2);

        Page<Item> itemsPage = itemRepository.searchItems("item", pageRequest);

        assertNotNull(itemsPage);
        assertFalse(itemsPage.isEmpty());
        assertEquals(2, itemsPage.getSize());
        assertEquals(item1, itemsPage.getContent().get(0));
        assertEquals(item2, itemsPage.getContent().get(1));
    }
}
