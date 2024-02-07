package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

@Data
@Builder
public class UserDto {
    @Null(groups = OnCreate.class, message = "Уникальный идентификатор задаётся автоматически")
    private Integer id;
    @NotBlank(groups = OnCreate.class, message = "Имя пользователя не должно быть пустым")
    private String name;
    @NotBlank(groups = OnCreate.class, message = "Email пользователя не должен быть пустым")
    @Email(message = "Введён некорректный email пользователя")
    private String email;
}
