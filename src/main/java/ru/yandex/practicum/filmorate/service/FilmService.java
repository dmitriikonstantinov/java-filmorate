package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    public void addLike(Long filmId, Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден!"));
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден!"));
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int rowsUpdate = jdbcTemplate.update(sql, filmId, userId);
        if (rowsUpdate == 0) {
            throw new NotFoundException("Пользователь не ставил лайк фильму!");
        }
        log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        String sql = """
                SELECT f.*, COUNT(fl.user_id) AS likes_count
                FROM films AS f
                LEFT JOIN film_likes AS fl ON f.id=fl.film_id
                GROUP BY f.id
                ORDER BY likes_count DESC
                LIMIT ?""";
        return jdbcTemplate.query(sql, filmRowMapper, count);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        if (film.getMpaRating() == null) {
            throw new NotFoundException("Рейтинг должен быть указан");
        }


        String ratingSql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
        Integer ratingCount = jdbcTemplate.queryForObject(ratingSql, Integer.class, film.getMpaRating().getId());
        if (ratingCount == 0) {
            throw new NotFoundException("Рейтинг с ID " + film.getMpaRating().getId() + " не найден");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                String genreSql = "SELECT COUNT(*) FROM genres WHERE id = ?";
                Integer genreCount = jdbcTemplate.queryForObject(genreSql, Integer.class, genre.getId());
                if (genreCount == 0) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
            }
        }

        validate(film);
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate()
                .isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть ранее 1895 года");
        }
        if (newFilm.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным  числом!");
        }
        return filmStorage.update(newFilm);
    }


    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    private boolean validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название должно быть указано!");
        }
        if (film.getDescription() == null || film.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым!");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Описание должно быть не более 200 символов!");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть ранее 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        return true;
    }
}
