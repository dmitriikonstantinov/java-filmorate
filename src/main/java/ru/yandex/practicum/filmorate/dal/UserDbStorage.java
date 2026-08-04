package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    protected final JdbcTemplate jdbcTemplate;
    protected final UserRowMapper mapper;

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, mapper);
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        long id = keyHolder.getKeyAs(Long.class);
        user.setId(id);
        return user;
    }

    @Override
    public User update(User newUser) {
        String sql = "UPDATE users SET email = ?, login = ?, name =?, birthday = ? WHERE id = ?";
        int rowsUpdate = jdbcTemplate.update(sql,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                java.sql.Date.valueOf(newUser.getBirthday()),
                newUser.getId());
        if (rowsUpdate == 0) {
            throw new NotFoundException("Пользователь не найден!");
        }

        return newUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, mapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(Long idUser, Long idFriend) {
        String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (? ,?)";
        jdbcTemplate.update(sql, idUser, idFriend);

    }

    @Override
    public void removeFriend(Long idUser, Long idFriend) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, idUser, idFriend);
    }

    @Override
    public List<User> userFriends(Long userId) {
        String sql = """
                SELECT u.*
                FROM users AS u
                JOIN friendship AS fh ON u.id=fh.friend_id
                WHERE user_id = ?
                """;
        return jdbcTemplate.query(sql, mapper, userId);
    }

    @Override
    public List<User> getCommonFriend(Long userId, Long friendId) {
        String sql = """
                SELECT u.*
                FROM users AS u
                JOIN friendship AS fh1 ON u.id=fh1.friend_id AND fh1.user_id = ?
                JOIN friendship AS fh2 ON u.id=fh2.friend_id AND fh2.user_id = ?""";
        return jdbcTemplate.query(sql, mapper, userId, friendId);
    }
}
