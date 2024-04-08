package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.*;

@Data
@Builder(toBuilder = true)
public class ItemDto {
    @Null(groups = OnCreate.class, message = "Id of item must set automatically")
    private Long id;

    @NotBlank(groups = OnCreate.class, message = "Name of item can't be empty")
    @Size(max = 255, message = "Name of the item must not exceed 255 characters")
    private String name;

    @NotBlank(groups = OnCreate.class, message = "Description of item can't be empty")
    @Size(max = 512, message = "Description of the item must not exceed 512 characters")
    private String description;

    @NotNull(groups = OnCreate.class, message = "Availability of item can't be empty")
    private Boolean available;

    @Positive(message = "Request id must be positive")
    private Long requestId;
}
