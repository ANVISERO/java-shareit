package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.util.mapper.UserMapper;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.groups.Default;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public User createUser(@Validated({OnCreate.class, Default.class}) @RequestBody UserDto userDto) {
        log.debug("POST request received to create a new user {}", userDto);
        User user = UserMapper.userDtoToUser(userDto);
        log.debug("The message body converted to an object {}", user);
        return userService.createUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("GET request received to get all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable(name = "userId") @Positive Long userId) {
        log.debug("GET request received to get user by id = {}", userId);
        return userService.getUserById(userId);
    }

    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable(name = "userId") @Positive Long userId,
                           @Valid @RequestBody UserDto userDto) {
        log.debug("PATCH request received to update user by id = {}", userId);
        User user = UserMapper.userDtoToUser(userDto);
        log.debug("The message body converted to an object {}", user);
        return userService.updateUser(userId, user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "userId") @Positive Long userId) {
        log.debug("DELETE request received to delete user by id = {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(String.format("User with id = %s successfully deleted", userId));
    }
}
