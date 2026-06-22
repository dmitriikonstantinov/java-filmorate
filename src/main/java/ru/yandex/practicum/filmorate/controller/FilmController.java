package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validate(film);
        film.setId(indexId());
        films.put(film.getId(), film);
        log.info("Создан фильм: {} (ID={})", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
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
            if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата релиза не может быть ранее 1895 года");
            }
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != 0) {
            if (newFilm.getDuration() <= 0) {
                throw new ValidationException("Продолжительность фильма должна быть положительным  числом!");
            }
            oldFilm.setDuration(newFilm.getDuration());
        }
        if (newFilm.getRate() != null) {
            oldFilm.setRate(newFilm.getRate());
        }

        log.info("Отредактирован фильм: {} (ID={})", oldFilm.getName(), oldFilm.getId());
        return oldFilm;
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

    private long indexId() {
        long maxIndexId = films.keySet().stream().mapToLong(n -> n).max().orElse(0);
        return ++maxIndexId;
    }
}
