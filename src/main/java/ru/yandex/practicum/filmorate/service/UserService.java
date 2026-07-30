package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public void addFriend(Long idUser, Long idFriend) {
        if (idUser == null) {
            throw new ValidationException("Нужно указать свой id!");
        }
        if (idFriend == null) {
            throw new ValidationException("Нужно указать id друга!");
        }
        userStorage.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        userStorage.findById(idFriend)
                .orElseThrow(() -> new NotFoundException("Друг не найден!"));

        String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (? ,?)";
        jdbcTemplate.update(sql, idUser, idFriend);

        log.info("Пользователь {} добавил друга {}", idUser, idFriend);
    }

    public void removeFriend(Long idUser, Long idFriend) {
        if (idUser == null) {
            throw new ValidationException("Нужно указать свой id!");
        }
        if (idFriend == null) {
            throw new ValidationException("Нужно указать id друга!");
        }
        userStorage.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        userStorage.findById(idFriend)
                .orElseThrow(() -> new NotFoundException("Друг не найден!"));

        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        int rowUpdated = jdbcTemplate.update(sql, idUser, idFriend);
        if (rowUpdated == 0) {
            throw new NotFoundException("Такого друга нет!");
        }
        log.info("Пользователь {} удалил друга {}", idUser, idFriend);
    }

    public List<User> userFriends(Long userId) {
        if (userId == null) {
            throw new ValidationException("Укажите id пользователя");
        }
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        String sql = """
                SELECT u.*
                FROM users AS u
                JOIN friendship AS fh ON u.id=fh.friend_id
                WHERE user_id = ?
                """;
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    public List<User> getCommonFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new ValidationException("Нужно указать id!");
        }
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        userStorage.findById(friendId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        String sql = """
                SELECT u.*
                FROM users AS u
                JOIN friendship AS fh1 ON u.id=fh1.friend_id AND fh1.user_id = ?
                JOIN friendship AS fh2 ON u.id=fh2.friend_id AND fh2.user_id = ?""";
        return jdbcTemplate.query(sql, userRowMapper, userId, friendId);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validate(user);
        boolean emailExist = userStorage.findAll().stream().anyMatch(e -> e.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailExist) {
            throw new ValidationException("Пользователь с таким email уже есть");
        }
        boolean loginExist = userStorage.findAll().stream().anyMatch(l -> l.getLogin().equalsIgnoreCase(user.getLogin()));
        if (loginExist) {
            throw new ValidationException("Пользователь с таким логином уже есть!");
        }
        return userStorage.create(user);
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new NotFoundException("ID должен быть указан!");
        }

        User oldUser = userStorage.findById(newUser.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + newUser.getId() + " не найден!"));

        if (newUser.getEmail() != null && !newUser.getEmail().equalsIgnoreCase(oldUser.getEmail())) {
            boolean emailExist = userStorage.findAll().stream()
                    .anyMatch(e -> !e.getId().equals(oldUser.getId()) && e.getEmail()
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
                boolean loginExist = userStorage.findAll().stream()
                        .anyMatch(l -> !l.getId().equals(oldUser.getId()) && l.getLogin()
                                .equalsIgnoreCase(newUser.getLogin()));
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
        return userStorage.update(oldUser);
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
}
