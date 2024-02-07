package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        return userRepository.createUser(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public User getUserById(Integer userId) {
        return userRepository.getUserById(userId);
    }

    @Override
    public User updateUser(Integer userId, User user) {
        user.setId(userId);
        return userRepository.updateUser(user);
    }

    @Override
    public void deleteUser(Integer userId) {
        userRepository.deleteUser(userId);
    }
}
