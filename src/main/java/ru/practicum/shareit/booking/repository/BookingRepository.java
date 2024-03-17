package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime now,
                                                                             LocalDateTime now1);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus bookingStatus);

    List<Booking> findAllByItemOwnerOrderByStartDesc(User owner);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime now, LocalDateTime now1);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus bookingStatus);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.start < CURRENT_TIMESTAMP AND NOT b.status = 'REJECTED' " +
            "ORDER BY b.end DESC")
    List<Booking> findUserLastBookingsForEachItem(@Param("ownerId") Long userId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.start > CURRENT_TIMESTAMP AND NOT b.status = 'REJECTED' " +
            "ORDER BY b.start ASC")
    List<Booking> findUserNextBookingsForEachItem(@Param("ownerId") Long userId);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.booker.id = :userId AND b.item.id = :itemId AND b.end < CURRENT_TIMESTAMP")
    List<Booking> findAllFinishedBookingsByUserAndItem(Long userId, Long itemId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.id = :itemId AND b.start < CURRENT_TIMESTAMP AND NOT b.status = 'REJECTED' " +
            "ORDER BY b.end DESC")
    List<Booking> findUserLastBooking(@Param("itemId") Long itemId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.item " +
            "WHERE b.item.id = :itemId AND b.start > CURRENT_TIMESTAMP AND NOT b.status = 'REJECTED' " +
            "ORDER BY b.start ASC")
    List<Booking> findUserNextBooking(@Param("itemId") Long itemId);
}
