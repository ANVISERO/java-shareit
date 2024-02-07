package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    User createUser(User user);

    List<User> getAllUsers();

    User getUserById(Integer userId);

    User updateUser(Integer userId, User user);

    void deleteUser(Integer userId);
}
