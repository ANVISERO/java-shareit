package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.IncorrectDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Slf4j
@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Integer, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private int userId = 0;

    @Override
    public User createUser(User user) {
        if (emails.contains(user.getEmail())) {
            log.warn("Email {} already exists", user.getEmail());
            throw new IncorrectDataException(String.format("Email $s already exists", user.getEmail()));
        }
        emails.add(user.getEmail());
        user.setId(generateUserId());
        log.debug("id = {} has been assigned to the new user", user.getId());
        users.put(user.getId(), user);
        log.debug("New user with id = {} successfully added to the storage",
                user.getId());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> receivedUsers = new ArrayList<>(users.values());
        log.debug("All users received, their total number {}", receivedUsers.size());
        return receivedUsers;
    }

    @Override
    public User getUserById(Integer userId) {
        checkUserExists(userId);
        log.debug("User with id = {} found", userId);
        User user = users.get(userId);
        log.debug("User with id = {} received}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        checkUserExists(user.getId());
        log.debug("User with id = {} found", user.getId());
        User previousUser = users.get(user.getId());
        if (user.getName() != null) {
            previousUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            if (emails.contains(user.getEmail()) && !user.getEmail().equals(previousUser.getEmail())) {
                log.warn("Email {} already exists", user.getEmail());
                throw new IncorrectDataException(String.format("Email $s already exists", user.getEmail()));
            } else {
                emails.remove(previousUser.getEmail());
                emails.add(user.getEmail());
            }
            previousUser.setEmail(user.getEmail());
        }
        users.put(previousUser.getId(), previousUser);
        log.debug("User with id = {} updated {}", previousUser.getId(), previousUser);
        return previousUser;
    }

    @Override
    public void deleteUser(Integer userId) {
        checkUserExists(userId);
        log.debug("User with id = {} found", userId);
        User user = users.remove(userId);
        emails.remove(user.getEmail());
        log.debug("User with id = {} successfully deleted", userId);
    }

    private Integer generateUserId() {
        return ++userId;
    }

    @Override
    public void checkUserExists(Integer userId) {
        if (!users.containsKey(userId)) {
            log.warn("User with id = {} not found", userId);
            throw new NotFoundException(String.format("User with id = %s not found", userId));
        }
    }
}