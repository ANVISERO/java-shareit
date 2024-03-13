package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@Builder
public class ItemDto {
    @Null(groups = OnCreate.class, message = "Id of item must set automatically")
    private Integer id;
    @NotBlank(groups = OnCreate.class, message = "Name of item can't be empty")
    private String name;
    @NotBlank(groups = OnCreate.class, message = "Description of item can't be empty")
    private String description;
    @NotNull(groups = OnCreate.class, message = "Availability of item can't be empty")
    private Boolean available;
    private Integer ownerId;
}
