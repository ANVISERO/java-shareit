package ru.practicum.shareit.user.util.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Component
public class UserMapper {
    public User userDtoToUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public UserDto userToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User updateUser(User existingUser, User user) {
        return User.builder()
                .id(existingUser.getId())
                .name(user.getName() != null ? user.getName() : existingUser.getName())
                .email(user.getEmail() != null ? user.getEmail() : existingUser.getEmail())
                .build();
    }
}
