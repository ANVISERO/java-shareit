package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

@Data
@Builder
public class User {
    @Null(groups = OnCreate.class, message = "Id of user must set automatically")
    private Integer id;
    @NotBlank(groups = OnCreate.class, message = "Name of user can't be empty")
    private String name;
    @NotBlank(groups = OnCreate.class, message = "Email of user can't be empty")
    @Email(message = "User's email is incorrect")
    private String email;
}
