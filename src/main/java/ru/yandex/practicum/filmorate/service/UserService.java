package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryUserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final InMemoryUserStorage userStorage;

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
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }
}
