package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    public User create(User user) {
        user.setId(indexId());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {} (ID={})", user.getName(), user.getId());
        return user;
    }


    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new NotFoundException("ID должен быть указан!");
        }
        User oldUser = users.get(user.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь с ID: " + user.getId() + " не найден!");
        }

        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }
        if (user.getLogin() != null) {
            oldUser.setLogin(user.getLogin());
        }
        if (user.getBirthday() != null) {
            oldUser.setBirthday(user.getBirthday());
        }

        log.info("Данные пользователя обновлены: {} (ID={})", oldUser.getLogin(), oldUser.getId());
        return oldUser;
    }


    private long indexId() {
        long maxIndexId = users.keySet().stream().mapToLong(n -> n).max().orElse(0);
        return ++maxIndexId;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
}
