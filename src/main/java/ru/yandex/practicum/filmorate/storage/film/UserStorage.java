package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    Optional<User> findById(Long id);

    void addFriend(Long idUser, Long idFriend);

    void removeFriend(Long idUser, Long idFriend);

    List<User> userFriends(Long userId);

    List<User> getCommonFriend(Long userId, Long friendId);
}
