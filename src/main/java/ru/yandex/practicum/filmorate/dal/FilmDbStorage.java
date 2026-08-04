package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    protected final JdbcTemplate jdbcTemplate;
    protected final FilmRowMapper mapper;
    protected final UserStorage userStorage;

    @Override
    public Collection<Film> findAll() {
        String sql = """
                SELECT 
                    f.id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    f.mpa_rating_id,
                    g.id AS genre_id,
                    g.name AS genre_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                LEFT JOIN film_genre fg ON f.id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.id
                ORDER BY f.id
                """;

        Map<Long, Film> filmMap = new LinkedHashMap<>();

        jdbcTemplate.query(sql, (rs) -> {
            Long filmId = rs.getLong("id");
            Film film = filmMap.get(filmId);
            if (film == null) {
                film = new Film();
                film.setId(filmId);
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));
                film.setReleaseDate(rs.getDate("release_date").toLocalDate());
                film.setDuration(rs.getInt("duration"));

                int mpaId = rs.getInt("mpa_rating_id");
                if (!rs.wasNull()) {
                    film.setMpaRating(MpaRating.fromId(mpaId));
                }

                filmMap.put(filmId, film);
            }

            Long genreId = rs.getLong("genre_id");
            if (!rs.wasNull() && genreId != 0) {
                String genreName = rs.getString("genre_name");
                film.getGenres().add(new Genre(genreId.intValue(), genreName));
            }
        });

        return filmMap.values();
    }

    @Override
    public Film create(Film film) {
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

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpaRating().getId());
            return ps;
        }, keyHolder);
        long id = keyHolder.getKeyAs(Long.class);
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(genreSql, id, genre.getId());
            }
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?" +
                "WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpaRating().getId(),
                film.getId());
        if (rowsUpdated == 0) {
            throw new NotFoundException("Фильм с ID: " + film.getId() + "  не найден!");
        }
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, mapper, id);
            String genreSql = "SELECT g.* FROM genres g JOIN film_genre fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
            List<Genre> genres = jdbcTemplate.query(genreSql, new GenreRowMapper(), id);
            film.setGenres(new HashSet<>(genres));
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        findById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден!"));
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден!"));
        findById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден!"));
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int rowsUpdate = jdbcTemplate.update(sql, filmId, userId);
        if (rowsUpdate == 0) {
            throw new NotFoundException("Пользователь не ставил лайк фильму!");
        }
        log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = """
        SELECT 
            f.id,
            f.name,
            f.description,
            f.release_date,
            f.duration,
            f.mpa_rating_id,
            m.name AS mpa_name,
            g.id AS genre_id,
            g.name AS genre_name,
            COUNT(fl.user_id) AS likes_count
        FROM films f
        LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
        LEFT JOIN film_likes fl ON f.id = fl.film_id
        LEFT JOIN film_genre fg ON f.id = fg.film_id
        LEFT JOIN genres g ON fg.genre_id = g.id
        GROUP BY f.id, m.name, g.id, g.name
        ORDER BY likes_count DESC
        LIMIT ?
        """;

        Map<Long, Film> filmMap = new LinkedHashMap<>();

        jdbcTemplate.query(sql, (rs) -> {
            Long filmId = rs.getLong("id");
            Film film = filmMap.get(filmId);
            if (film == null) {
                film = new Film();
                film.setId(filmId);
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));
                film.setReleaseDate(rs.getDate("release_date").toLocalDate());
                film.setDuration(rs.getInt("duration"));

                int mpaId = rs.getInt("mpa_rating_id");
                if (!rs.wasNull()) {
                    film.setMpaRating(MpaRating.fromId(mpaId));
                }

                filmMap.put(filmId, film);
            }

            Long genreId = rs.getLong("genre_id");
            if (!rs.wasNull() && genreId != 0) {
                String genreName = rs.getString("genre_name");
                film.getGenres().add(new Genre(genreId.intValue(), genreName));
            }
        }, count);

        return new ArrayList<>(filmMap.values());
    }

}
