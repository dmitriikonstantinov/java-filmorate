package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JdbcTest
@ComponentScan("ru.yandex.practicum.filmorate.dal")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("ivan@mail.ru");
        user.setName("Ivan");
        user.setLogin("Vano");
        user.setBirthday(LocalDate.of(2005, 5, 11));

        User savedUser = userStorage.create(user);

        assertThat(savedUser.getId()).isPositive();
        assertThat(savedUser.getEmail()).isEqualTo("ivan@mail.ru");
    }

    @Test
    void shouldFindUserById() {
        User user = new User();
        user.setEmail("petr@mail.ru");
        user.setName("Petr");
        user.setLogin("rocket");
        user.setBirthday(LocalDate.of(2006, 11, 5));
        User savedUser = userStorage.create(user);

        Optional<User> foundUser = userStorage.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("petr@mail.ru");
    }

    @Test
    void whenUserNotFound() {
        User user = new User();
        user.setEmail("petr@mail.ru");
        user.setName("Petr");
        user.setLogin("rocket");
        user.setBirthday(LocalDate.of(2006, 11, 5));
        userStorage.create(user);
        Optional<User> userFound = userStorage.findById(2L);
        assertThat(userFound).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        User user = new User();
        user.setEmail("petr@mail.ru");
        user.setName("Petr");
        user.setLogin("rocket");
        user.setBirthday(LocalDate.of(2006, 11, 5));
        userStorage.create(user);

        User user2 = new User();
        user2.setEmail("ivan@mail.ru");
        user2.setName("Ivan");
        user2.setLogin("Vano");
        user2.setBirthday(LocalDate.of(2005, 5, 11));
        userStorage.create(user2);

        List<User> allUsers = userStorage.findAll().stream().toList();

        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getEmail)
                .contains("ivan@mail.ru", "petr@mail.ru");

    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("ivan@mail.ru");
        user.setName("Ivan");
        user.setLogin("Vano");
        user.setBirthday(LocalDate.of(2005, 5, 11));
        User savedUser = userStorage.create(user);

        savedUser.setEmail("petr@mail.ru");
        savedUser.setName("Petr");
        savedUser.setLogin("rocket");

        User updatedUser = userStorage.update(savedUser);

        assertThat(updatedUser.getEmail()).isEqualTo("petr@mail.ru");
        assertThat(updatedUser.getName()).isEqualTo("Petr");
        assertThat(updatedUser.getLogin()).isEqualTo("rocket");

        Optional<User> foundUser = userStorage.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("petr@mail.ru");
    }
}
