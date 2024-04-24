package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

    public Map<Long, User> users;
    private Long currentId;

    public InMemoryUserStorage() {
        currentId = 0L;
        users = new HashMap<>();
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {

        if (users.values().stream().noneMatch(u -> u.getEmail().equals(user.getEmail()))) {
            if (isValidUser(user)) {
                user.setId(++currentId);
                users.put(user.getId(), user);
            }
        } else {
            throw new UserAlreadyExistException("Пользователь с email " + user.getEmail() + " уже существует");
        }
        return user;
    }

    @Override
    public User update(User user) {

        Long userId = user.getId();
        if (userId == null) {
            throw new ValidationException("Идентификатор пользователя не может быть пустым");
        }
        User existingUser = users.get(userId);
        if (existingUser == null) {
            throw new UserNotFoundException("Пользователь с идентификатором " + userId + " не найден");
        }
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            if (!user.getEmail().equals(existingUser.getEmail()) && users.values().stream()
                    .filter(u -> !u.getId().equals(userId))
                    .anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
                throw new UserAlreadyExistException("Пользователь с email " + user.getEmail() + " уже существует");
            }
            existingUser.setEmail(user.getEmail());
        }
        users.put(userId, existingUser);
        return existingUser;
    }


    @Override
    public User getUserById(Long userId) {

        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с идентификатором " + userId + " не найден!");
        }
        return users.get(userId);
    }

    @Override
    public User delete(Long userId) {

        if (userId == null) {
            throw new ValidationException("Не может быть пустым");
        }
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с идентификатором " + userId + " не найден");
        }
        return users.remove(userId);
    }

    private boolean isValidUser(User user) {

        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }
        if (user.getEmail() == null) {
            throw new ValidationException("Email пользователя не может быть null");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email пользователя: " + user.getEmail());
        }
        if (user.getName() == null || user.getName().isEmpty() || user.getName().contains(" ")) {
            throw new ValidationException("Некорректный логин пользователя: " + user.getName());
        }
        return true;
    }
}
