package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        film.setId(indexId());
        films.put(film.getId(), film);
        log.info("Создан фильм: {} (ID={})", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new NotFoundException("Должен быть указан ID!");
        }
        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Фильм с таким ID: " + newFilm.getId() + " не найден!");
        }
        if (newFilm.getName() != null) {
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != 0) {
            oldFilm.setDuration(newFilm.getDuration());
        }
        if (newFilm.getRate() != null) {
            oldFilm.setRate(newFilm.getRate());
        }

        log.info("Отредактирован фильм: {} (ID={})", oldFilm.getName(), oldFilm.getId());
        return oldFilm;
    }



    private long indexId() {
        long maxIndexId = films.keySet().stream().mapToLong(n -> n).max().orElse(0);
        return ++maxIndexId;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }
}
