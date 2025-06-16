package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbc;

    private Film film1;
    private Film film2;
    private Film film3;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM film_genre");
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");

        film1 = new Film(0, "name1", "description1", LocalDate.now(), 60, new Mpa(1));
        film2 = new Film(0, "name2", "description2", LocalDate.now(), 60, new Mpa(2));
        film3 = new Film(0, "name3", "description3", LocalDate.now(), 60, new Mpa(3));

        user1 = new User(0, "test1@mail.ru", "Login1", "Name1", LocalDate.now());
        user2 = new User(0, "test2@mail.ru", "Login2", "Name2", LocalDate.now());
        user3 = new User(0, "test3@mail.ru", "Login3", "Name3", LocalDate.now());

        film1.addGenre(new Genre(1, "Комедия"));
        film1.addGenre(new Genre(3, "Мультфильм"));
        film1.addGenre(new Genre(5, "Документальный"));
        film2.addGenre(new Genre(3, "Мультфильм"));
        film3.addGenre(new Genre(5, "Документальный"));

        film1 = filmDbStorage.save(film1);
        film2 = filmDbStorage.save(film2);
        film3 = filmDbStorage.save(film3);
    }

    @Test
    void getAll() {
        user1 = userDbStorage.save(user1);
        filmDbStorage.saveId(film1.getId(), user1.getId());
        filmDbStorage.saveId(film2.getId(), user1.getId());
        filmDbStorage.saveId(film3.getId(), user1.getId());

        final Collection<Film> films = filmDbStorage.getAll();

        assertThat(films.size()).isEqualTo(3);
        films.forEach(
                film -> {
                    assertFalse(film.getGenres().isEmpty());
                    assertNotNull(film.getMpa());
                    assertThat(film.getIds().size()).isEqualTo(1);
                });
    }

    @Test
    void getById() {
        user1 = userDbStorage.save(user1);
        Film film4 = new Film(0, "name4", "description4", LocalDate.now(), 60, new Mpa(1));
        film4.addGenre(new Genre(5, "Документальный"));
        film4 = filmDbStorage.save(film4);
        filmDbStorage.saveId(film4.getId(), user1.getId());
        final Film film = filmDbStorage.getById(film4.getId());

        assertThat(film).hasFieldOrPropertyWithValue("id", film4.getId());
        assertThat(film).hasFieldOrPropertyWithValue("name", film4.getName());
        assertThat(film).hasFieldOrPropertyWithValue("description", film4.getDescription());
        assertThat(film).hasFieldOrPropertyWithValue("releaseDate", film4.getReleaseDate());
        assertThat(film).hasFieldOrPropertyWithValue("duration", film4.getDuration());

        assertThat(film.getMpa().getId()).isEqualTo(film4.getMpa().getId());
        assertThat(film.getGenres().size()).isEqualTo(film4.getGenres().size());
        final Genre genre = film.getGenres().stream().toList().get(0);
        assertThat(genre).hasFieldOrPropertyWithValue("id", 5);
        assertThat(genre).hasFieldOrPropertyWithValue("name", "Документальный");
        assertThat(film.getIds().size()).isEqualTo(1);

        assertThrows(NotFoundException.class, () -> filmDbStorage.getById(100));
    }

    @Test
    void saveUpdatedObject() {
        film1.setName("updName");
        film1.setDescription("updDescription");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(120);
        film1.setMpa(new Mpa(5));
        film1.addGenre(new Genre(2, "Драма"));

        filmDbStorage.saveUpdatedObject(film1);

        final Film updFilm = filmDbStorage.getById(film1.getId());

        assertThat(updFilm).hasFieldOrPropertyWithValue("id", film1.getId());
        assertThat(updFilm).hasFieldOrPropertyWithValue("name", film1.getName());
        assertThat(updFilm).hasFieldOrPropertyWithValue("description", film1.getDescription());
        assertThat(updFilm).hasFieldOrPropertyWithValue("releaseDate", film1.getReleaseDate());
        assertThat(updFilm).hasFieldOrPropertyWithValue("duration", film1.getDuration());

        assertThat(updFilm.getMpa().getId()).isEqualTo(film1.getMpa().getId());
        assertThat(updFilm.getGenres().size()).isEqualTo(film1.getGenres().size());
    }

    @Test
    void saveIdAndRemoveId() {
        user1 = userDbStorage.save(user1);
        Film film4 = new Film(0, "name4", "description4", LocalDate.now(), 60, new Mpa(1));
        film4 = filmDbStorage.save(film4);
        Film film = filmDbStorage.saveId(film4.getId(), user1.getId());
        assertThat(film.getIds().size()).isEqualTo(1);
        assertThat(film.getIds().contains(user1.getId())).isTrue();
        Film finalFilm = film4;
        assertThrows(DuplicatedDataException.class, () -> filmDbStorage.saveId(finalFilm.getId(), user1.getId()));

        film = filmDbStorage.removeId(film4.getId(), user1.getId());
        assertThat(film.getIds().size()).isEqualTo(0);
    }

    @Test
    void findTheMostPopular() {
        user1 = userDbStorage.save(user1);
        user2 = userDbStorage.save(user2);
        user3 = userDbStorage.save(user3);

        filmDbStorage.saveId(film3.getId(), user1.getId());
        filmDbStorage.saveId(film3.getId(), user2.getId());
        filmDbStorage.saveId(film3.getId(), user3.getId());
        filmDbStorage.saveId(film2.getId(), user1.getId());
        filmDbStorage.saveId(film2.getId(), user2.getId());
        filmDbStorage.saveId(film1.getId(), user1.getId());

        final List<Film> mostPopular = (List<Film>) filmDbStorage.findTheMostPopular(3);

        assertThat(mostPopular.get(0)).hasFieldOrPropertyWithValue("id", film3.getId());
        assertThat(mostPopular.get(1)).hasFieldOrPropertyWithValue("id", film2.getId());
        assertThat(mostPopular.get(2)).hasFieldOrPropertyWithValue("id", film1.getId());

        final Film film = mostPopular.get(0);

        assertThat(film).hasFieldOrPropertyWithValue("id", film3.getId());
        assertThat(film).hasFieldOrPropertyWithValue("name", film3.getName());
        assertThat(film).hasFieldOrPropertyWithValue("description", film3.getDescription());
        assertThat(film).hasFieldOrPropertyWithValue("releaseDate", film3.getReleaseDate());
        assertThat(film).hasFieldOrPropertyWithValue("duration", film3.getDuration());
        assertThat(film.getMpa().getId()).isEqualTo(film3.getMpa().getId());
        assertThat(film.getGenres().size()).isEqualTo(film3.getGenres().size());
        assertThat(film.getIds().size()).isEqualTo(3);
    }
}