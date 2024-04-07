package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = {"booker", "item"})
    Optional<Booking> findById(Long bookingId);

    @EntityGraph(attributePaths = {"booker", "item"})
    Page<Booking> findByBookerId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.booker.id = :bookerId AND b.start < CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP")
    Page<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.booker.id = :bookerId AND b.end < CURRENT_TIMESTAMP")
    Page<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.booker.id = :bookerId AND b.start > CURRENT_TIMESTAMP")
    Page<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    Page<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus bookingStatus,
                                                             Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    Page<Booking> findAllByItemOwnerOrderByStartDesc(User owner, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = :ownerId AND b.start < CURRENT_TIMESTAMP AND b.end > CURRENT_TIMESTAMP")
    Page<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = :ownerId AND b.end < CURRENT_TIMESTAMP")
    Page<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query("SELECT b FROM Booking AS b " +
            "WHERE b.item.owner.id = :ownerId AND b.start > CURRENT_TIMESTAMP")
    Page<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    Page<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus bookingStatus,
                                                                Pageable pageable);

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
