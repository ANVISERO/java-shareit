package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class ItemInfoDto {
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private BookingDto lastBooking;

    private BookingDto nextBooking;

    private List<CommentDto> comments;

    @Builder
    @Data
    public static class BookingDto {
        private Long id;

        private BookingStatus status;

        private LocalDateTime start;

        private LocalDateTime end;

        private Long itemId;

        private Long bookerId;
    }
}
