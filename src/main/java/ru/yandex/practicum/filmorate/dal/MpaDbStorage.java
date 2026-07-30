package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingRowMapper mapper;

    public List<MpaRating> findAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, mapper);
    }

    public Optional<MpaRating> findById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            MpaRating rating = jdbcTemplate.queryForObject(sql, mapper, id);
            return Optional.ofNullable(rating);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
