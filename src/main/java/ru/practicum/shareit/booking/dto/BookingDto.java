package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    @Null(groups = OnCreate.class, message = "Id of booking must set automatically")
    private Long id;
    private BookingStatus status;
    @NotNull(groups = OnCreate.class, message = "Booking start time can't be empty")
    @Future(message = "You can't book a date in the past")
    private LocalDateTime start;
    @NotNull(groups = OnCreate.class, message = "Booking end time can't be empty")
    @Future(message = "You can't book a date in the past")
    private LocalDateTime end;
    @Positive
    private Long itemId;
    @Positive
    private Long bookerId;
    private UserDto booker;
    private ItemDto item;
}
