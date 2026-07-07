package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {
    private final InMemoryFilmStorage filmStorage;

    public void addLike(Long filmId, Long userId) {
        if (filmId == null) {
            throw new ValidationException("Должен быть указан id фильма!");
        }
        if (userId == null) {
            throw new ValidationException("Должен быть указан id пользователя!");
        }
        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден!"));
        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        if (filmId == null) {
            throw new ValidationException("Должен быть указан id фильма!");
        }
        if (userId == null) {
            throw new ValidationException("Должен быть указан id пользователя!");
        }
        Film film = filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден!"));
        if (!film.getLikes().remove(userId)) {
            throw new NotFoundException("Пользователь не ставил лайк этому фильму");
        }
        log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }
}
