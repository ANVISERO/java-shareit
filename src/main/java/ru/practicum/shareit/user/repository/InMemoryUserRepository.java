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
    private static final Map<Integer, User> users = new HashMap<>();
    private static final Set<String> emails = new HashSet<>();
    private static int userId = 0;

    @Override
    public User createUser(User user) {
        if (emails.contains(user.getEmail())) {
            log.warn("Email {} уже существует", user.getEmail());
            throw new IncorrectDataException(String.format("Email $s уже существует", user.getEmail()));
        }
        emails.add(user.getEmail());
        user.setId(generateUserId());
        log.debug("Новому пользователю присвоен уникальный идентификатор id = {}", user.getId());
        users.put(user.getId(), user);
        log.debug("Новый пользователь с идентификатором id = {} успешно добавлен в хранилище",
                user.getId());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> receivedUsers = new ArrayList<>(users.values());
        log.debug("Все пользователи получены, их общее количество {}", receivedUsers.size());
        return receivedUsers;
    }

    @Override
    public User getUserById(Integer userId) {
        checkUserExists(userId);
        log.debug("Пользователь с идентификатором id = {} найден", userId);
        User user = users.get(userId);
        log.debug("Получен пользователь с уникальным идентификатором id = {}", userId);
        return user;
    }

    @Override
    public User updateUser(User user) {
        checkUserExists(user.getId());
        log.debug("Пользователь с идентификатором id = {} найден", user.getId());
        User previousUser = users.get(user.getId());
        if (user.getName() != null) {
            previousUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            if (emails.contains(user.getEmail()) && !user.getEmail().equals(previousUser.getEmail())) {
                log.warn("Email {} уже существует", user.getEmail());
                throw new IncorrectDataException(String.format("Email $s уже существует", user.getEmail()));
            } else {
                emails.remove(previousUser.getEmail());
                emails.add(user.getEmail());
            }
            previousUser.setEmail(user.getEmail());
        }
        users.put(previousUser.getId(), previousUser);
        log.debug("Пользователь с уникальным идентификатором id = {} обновлён {}", previousUser.getId(), previousUser);
        return previousUser;
    }

    @Override
    public void deleteUser(Integer userId) {
        checkUserExists(userId);
        log.debug("Пользователь с идентификатором id = {} найден", userId);
        User user = users.remove(userId);
        emails.remove(user.getEmail());
        log.debug("Пользователь с уникальным идентификатором id = {} успешно удалён", userId);
    }

    private static Integer generateUserId() {
        return ++userId;
    }

    @Override
    public void checkUserExists(Integer userId) {
        if (!users.containsKey(userId)) {
            log.warn("Пользователь с идентификатором id = {} не найден", userId);
            throw new NotFoundException(String.format("Пользователь с идентификатором id = %s не найден", userId));
        }
    }
}