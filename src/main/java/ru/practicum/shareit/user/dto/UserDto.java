package ru.practicum.shareit.user.dto;

import lombok.*;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

@Data
@Builder(toBuilder = true)
public class UserDto {
    @Null(groups = OnCreate.class, message = "Id of user must set automatically")
    private Long id;

    @NotBlank(groups = OnCreate.class, message = "Name of user can't be empty")
    @Size(max = 255, message = "Name of user must not exceed 255 characters")
    private String name;

    @NotBlank(groups = OnCreate.class, message = "Email of user can't be empty")
    @Email(message = "User's email is incorrect")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
}
