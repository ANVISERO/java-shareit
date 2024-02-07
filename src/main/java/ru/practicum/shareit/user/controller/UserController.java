package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.Valid;
import javax.validation.groups.Default;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public User createUser(@Validated({OnCreate.class, Default.class}) @RequestBody UserDto userDto) {
        log.debug("Получен Post запрос для создания нового пользователя {}", userDto);
        User user = UserMapper.userDtoToUser(userDto);
        log.debug("Тело сообщения преобразовано в объект {}", user);
        return userService.createUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Получен Get запрос для получения всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable(name = "userId") Integer userId) {
        log.debug("Получен Get запрос для получения пользователя по его уникальному идентификатору");
        return userService.getUserById(userId);
    }

    @PatchMapping("/{userId}")
    public User updateUser(@PathVariable(name = "userId") Integer userId,
                           @Valid @RequestBody UserDto userDto) {
        log.debug("Получен Patch запрос для обновления пользователя по его уникальному идентификатору id = {}", userId);
        User user = UserMapper.userDtoToUser(userDto);
        log.debug("Тело сообщения преобразовано в объект {}", user);
        return userService.updateUser(userId, user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "userId") Integer userId) {
        log.debug("Получен Delete запрос для удаления пользователя по его уникальному идентификатору");
        userService.deleteUser(userId);
        return ResponseEntity.ok(String.format("Пользователь с уникальным идентификатором id = %s успешно удалён",
                userId));
    }
}
