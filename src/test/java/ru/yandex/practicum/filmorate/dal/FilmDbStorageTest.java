package ru.yandex.practicum.filmorate.dal;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JdbcTest
@ComponentScan("ru.yandex.practicum.filmorate.dal")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;

    @Test
    void shouldFilmCreate() {
        Film film = new Film();
        film.setName("Thing");
        film.setDuration(120);
        film.setRating(MpaRating.NC_17);
        film.setReleaseDate(LocalDate.of(1998, 4, 25));

        Film savedFilm = filmStorage.create(film);
        assertThat(savedFilm.getId()).isPositive();
        assertThat(savedFilm.getName()).isEqualTo("Thing");
    }

    @Test
    void shouldFindFilmById() {
        Film film = new Film();
        film.setName("Thing");
        film.setDuration(120);
        film.setRating(MpaRating.NC_17);
        film.setReleaseDate(LocalDate.of(1998, 4, 25));

        Film savedFilm = filmStorage.create(film);

        Optional<Film> findFilm = filmStorage.findById(savedFilm.getId());

        assertThat(findFilm).isPresent();
    }

    @Test
    void whenFilmNotFound() {
        Optional<Film> filmFound = filmStorage.findById(123L);
        assertThat(filmFound).isEmpty();
    }

    @Test
    void shouldFindAllFilms() {
        Film film = new Film();
        film.setName("Thing");
        film.setDuration(120);
        film.setRating(MpaRating.NC_17);
        film.setReleaseDate(LocalDate.of(1998, 4, 25));
        filmStorage.create(film);

        Film film2 = new Film();
        film2.setName("Alone in the dark");
        film2.setDuration(123);
        film2.setRating(MpaRating.NC_17);
        film2.setReleaseDate(LocalDate.of(2003, 7, 22));
        filmStorage.create(film2);

        List<Film> allFilms = filmStorage.findAll().stream().toList();

        assertThat(allFilms).hasSize(2);
        assertThat(allFilms).extracting(Film::getName)
                .contains("Thing", "Alone in the dark");

    }

    @Test
    void whenUpdateFilm() {
        Film film = new Film();
        film.setName("Thing");
        film.setDuration(120);
        film.setRating(MpaRating.NC_17);
        film.setReleaseDate(LocalDate.of(1998, 4, 25));

        Film savedFilm = filmStorage.create(film);

        savedFilm.setName("It");
        savedFilm.setReleaseDate(LocalDate.of(2015, 5, 3));

        Film updateFilm = filmStorage.update(savedFilm);

        assertThat(updateFilm.getName()).isEqualTo("It");
        assertThat(updateFilm.getReleaseDate()).isEqualTo(LocalDate.of(2015, 5, 3));


    }

}
