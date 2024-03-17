package ru.practicum.shareit.booking.util.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.util.mapper.ItemMapper;
import ru.practicum.shareit.user.util.mapper.UserMapper;

@UtilityClass
public class BookingMapper {
    public Booking bookingDtoToBooking(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .status(bookingDto.getStatus())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
    }

    public BookingDto bookingToBookingDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .status(booking.getStatus())
                .start(booking.getStart())
                .end(booking.getEnd())
                .booker(UserMapper.userToUserDto(booking.getBooker()))
                .item(ItemMapper.itemToItemDto(booking.getItem()))
                .build();
    }


    public BookingDto bookingToBookingDtoWithoutUserAndItem(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .status(booking.getStatus())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}
