package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.id = :bookingId")
    Optional<Booking> findByIdWithUserAndItem(Long bookingId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.booker.id = :userId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdOrderByStartDesc(Long userId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.booker.id = :bookerId AND b.start < CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.booker.id = :bookerId AND b.end < CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.booker.id = :bookerId AND b.start > CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.booker.id = :bookerId AND b.status = :bookingStatus " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus bookingStatus);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner = :owner " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerOrderByStartDesc(User owner);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.start < CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.end < CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.start > CURRENT_TIMESTAMP " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.booker " +
            "JOIN FETCH b.item " +
            "WHERE b.item.owner.id = :ownerId AND b.status = :bookingStatus " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus bookingStatus);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.item " +
            "JOIN FETCH b.booker " +
            "WHERE b.item.owner.id = :ownerId AND b.start < CURRENT_TIMESTAMP AND NOT b.status = 'REJECTED' " +
            "ORDER BY b.end DESC")
    List<Booking> findUserLastBookingsForEachItem(@Param("ownerId") Long userId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.item " +
            "JOIN FETCH b.booker " +
            "WHERE b.item.owner.id = :ownerId AND b.start > CURRENT_TIMESTAMP AND NOT b.status = 'REJECTED' " +
            "ORDER BY b.start ASC")
    List<Booking> findUserNextBookingsForEachItem(@Param("ownerId") Long userId);

    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.booker.id = :userId AND b.item.id = :itemId AND b.end < CURRENT_TIMESTAMP")
    List<Booking> findAllFinishedBookingsByUserAndItem(Long userId, Long itemId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.item " +
            "JOIN FETCH b.booker " +
            "WHERE b.item.id = :itemId AND b.start < CURRENT_TIMESTAMP AND NOT b.status = 'REJECTED' " +
            "ORDER BY b.end DESC")
    List<Booking> findUserLastBooking(@Param("itemId") Long itemId);

    @Query("SELECT b FROM Booking AS b " +
            "JOIN FETCH b.item " +
            "JOIN FETCH b.booker " +
            "WHERE b.item.id = :itemId AND b.start > CURRENT_TIMESTAMP AND NOT b.status = 'REJECTED' " +
            "ORDER BY b.start ASC")
    List<Booking> findUserNextBooking(@Param("itemId") Long itemId);
}
