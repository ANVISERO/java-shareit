package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class ItemRequestInfoDto {
    private Long id;

    private String description;

    private LocalDateTime created;

    private List<ItemDto> items;

    @Data
    @Builder(toBuilder = true)
    public static class ItemDto {
        private Long id;

        private String name;

        private String description;

        private Boolean available;

        private Long requestId;
    }
}
