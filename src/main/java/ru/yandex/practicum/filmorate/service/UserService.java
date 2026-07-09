package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;

    public void addFriend(Long idUser, Long idFriend) {
        if (idUser == null) {
            throw new ValidationException("Нужно указать свой id!");
        }
        if (idFriend == null) {
            throw new ValidationException("Нужно указать id друга!");
        }
        User user = userStorage.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        User friend = userStorage.findById(idFriend)
                .orElseThrow(() -> new NotFoundException("Друг не найден!"));

        user.getFriends().add(idFriend);
        friend.getFriends().add(idUser);

        log.info("Пользователь {} добавил друга {}", idUser, idFriend);
    }

    public void removeFriend(Long idUser, Long idFriend) {
        if (idUser == null) {
            throw new ValidationException("Нужно указать свой id!");
        }
        if (idFriend == null) {
            throw new ValidationException("Нужно указать id друга!");
        }
        User user = userStorage.findById(idUser)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        User friend = userStorage.findById(idFriend)
                .orElseThrow(() -> new NotFoundException("Друг не найден!"));

        user.getFriends().remove(idFriend);
        friend.getFriends().remove(idUser);
        log.info("Пользователь {} удалил друга {}", idUser, idFriend);
    }

    public List<User> userFriends(Long userId) {
        if (userId == null) {
            throw new ValidationException("Укажите id пользователя");
        }
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        Set<Long> idFriends = user.getFriends();
        return idFriends.stream().map(userStorage::findById).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
    }

    public List<User> getCommonFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new ValidationException("Нужно указать id!");
        }
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        User otherUser = userStorage.findById(friendId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        Set<Long> common = new HashSet<>(user.getFriends());
        common.retainAll(otherUser.getFriends());
        return common.stream().map(userStorage::findById).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
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
