package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
public class User {
    @Null(groups = OnCreate.class, message = "Уникальный идентификатор задаётся автоматически")
    private Integer id;
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    private String name;
    @NotBlank(message = "Email пользователя не должен быть пустым")
    @Email(message = "Введён некорректный email пользователя")
    private String email;
}
