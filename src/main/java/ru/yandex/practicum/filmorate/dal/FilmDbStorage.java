package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    protected final JdbcTemplate jdbcTemplate;
    protected final FilmRowMapper mapper;

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, mapper);

        String genreSql = "SELECT g.* FROM genres g JOIN film_genre fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        for (Film film : films) {
            List<Genre> genres = jdbcTemplate.query(genreSql, new GenreRowMapper(), film.getId());
            film.setGenres(new HashSet<>(genres));
        }

        return films;
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id)" +
                "VALUES (?, ?, ?, ?, ?)";
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
    public List<Film> findByMpaRatingId(int mpaRatingId) {
        String sql = "SELECT * FROM films WHERE mpa_rating_id = ?";
        return jdbcTemplate.query(sql, mapper, mpaRatingId);
    }
}
