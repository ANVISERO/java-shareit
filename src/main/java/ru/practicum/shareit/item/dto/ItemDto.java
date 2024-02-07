package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
public class ItemDto {
    @Null(groups = OnCreate.class, message = "Уникальный идентификатор задаётся автоматически")
    private Integer id;
    @NotBlank(groups = OnCreate.class, message = "Название вещи не может быть пустым")
    private String name;
    @NotBlank(groups = OnCreate.class, message = "Описание вещи не может быть пустым")
    private String description;
    @NotNull(groups = OnCreate.class, message = "Поле available не может быть пустым")
    private Boolean available;
    private Integer ownerId;
}
