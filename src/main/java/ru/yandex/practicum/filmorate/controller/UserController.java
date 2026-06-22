package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validate(user);
        user.setId(indexId());
        boolean emailExist = users.values().stream().anyMatch(e -> e.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExist) {
            throw new ValidationException("Пользователь с таким email уже есть");
        }
        boolean loginExist = users.values().stream().anyMatch(l -> l.getLogin().equalsIgnoreCase(user.getLogin()));
        if (loginExist) {
            throw new ValidationException("Пользователь с таким логином уже есть!");
        }
        users.put(user.getId(), user);
        log.info("Создан пользователь: {} (ID={})", user.getName(), user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new NotFoundException("ID должен быть указан!");
        }
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь с ID: " + newUser.getId() + " не найден!");
        }
        if (newUser.getEmail() != null && !newUser.getEmail().equalsIgnoreCase(oldUser.getEmail())) {
            boolean emailExist = users.values().stream().anyMatch(e -> !e.getId().equals(oldUser.getId())
                    && e.getEmail()
                    .equalsIgnoreCase(newUser.getEmail()));
            if (emailExist) {
                throw new ValidationException("Пользователь с таким email уже есть");
            }
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getLogin() != null) {
            if (newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
                throw new ValidationException("Логин не может быть пустым или содержать пробелы!");
            }

            if (!newUser.getLogin().equalsIgnoreCase(oldUser.getLogin())) {
                boolean loginExist = users.values().stream().anyMatch(l -> !l.getId().equals(oldUser.getId()) &&
                        l.getLogin().equalsIgnoreCase(newUser.getLogin()));
                if (loginExist) {
                    throw new ValidationException("Пользователь с таким логином уже есть!");
                }
                oldUser.setLogin(newUser.getLogin());
            }
        }
        if (newUser.getBirthday() != null) {
            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть позже текущей даты!");
            }
            oldUser.setBirthday(newUser.getBirthday());
        }

        log.info("Данные пользователя обновлены: {} (ID={})", oldUser.getLogin(), oldUser.getId());
        return oldUser;
    }

    private boolean validate(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Логин не может быть пустым!");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы!");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения должна быть указана!");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть позже текущего времени!");
        }

        return true;
    }

    private long indexId() {
        long maxIndexId = users.keySet().stream().mapToLong(n -> n).max().orElse(0);
        return ++maxIndexId;
    }

}
