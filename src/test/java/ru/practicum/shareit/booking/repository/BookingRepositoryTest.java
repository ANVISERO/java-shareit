package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.ShareItPageRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    User user1;
    User user2;
    Item item1;

    @BeforeEach
    void setUp() {
        user1 = User.builder().name("user1").email("user1@post.com").build();
        userRepository.save(user1);
        user2 = User.builder().name("user2").email("user2@post.com").build();
        userRepository.save(user2);
        item1 = Item.builder().name("item1").description("itemDescription1")
                .available(Boolean.TRUE).owner(user1).build();
        itemRepository.save(item1);
    }

    @AfterEach
    void clearDatabase() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    @DisplayName("findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc_whenInvoked_thenPageOfBookingsReturned")
    void findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc_whenInvoked_thenPageOfBookingsReturned() {
        Booking booking = Booking.builder().status(BookingStatus.WAITING).start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1)).booker(user2).item(item1).build();
        bookingRepository.save(booking);
        ShareItPageRequest pageRequest = new ShareItPageRequest(0, 1);

        Page<Booking> bookingPage = bookingRepository
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(2L, pageRequest);

        assertNotNull(bookingPage);
        assertFalse(bookingPage.isEmpty());
        assertEquals(1, bookingPage.getContent().size());
        assertEquals(booking, bookingPage.getContent().get(0));
    }
}
