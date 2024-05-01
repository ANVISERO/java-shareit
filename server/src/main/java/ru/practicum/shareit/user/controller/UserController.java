package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.util.mapper.UserMapper;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.debug("POST request received to create a new user {}", userDto);
        User user = userMapper.userDtoToUser(userDto);
        log.debug("The message body converted to an object {}", user);
        return userService.createUser(user);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.debug("GET request received to get all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable(name = "userId") Long userId) {
        log.debug("GET request received to get user by id = {}", userId);
        return userService.getUserById(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable(name = "userId") Long userId, @RequestBody UserDto userDto) {
        log.debug("PATCH request received to update user by id = {}", userId);
        User user = userMapper.userDtoToUser(userDto);
        log.debug("The message body converted to an object {}", user);
        return userService.updateUser(userId, user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable(name = "userId") Long userId) {
        log.debug("DELETE request received to delete user by id = {}", userId);
        userService.deleteUser(userId);
    }
}
