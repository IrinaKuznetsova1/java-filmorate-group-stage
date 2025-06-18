package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.mappers.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class,
        GenreDbStorage.class, GenreRowMapper.class,
        MpaDbStorage.class, MpaRowMapper.class,
        LikesDbStorage.class, LikeRowMapper.class,
        FilmGenreDbStorage.class, FilmGenreRowMapper.class,
        UserDbStorage.class, UserRowMapper.class,
        FriendsDbStorage.class, FriendsRowMapper.class,
        FilmDirectorDbStorage.class, FilmDirectorRowMapper.class,
        DirectorDbStorage.class, DirectorRowMapper.class})
class FilmDbStorageTest {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbc;

    private final Film film1 = new Film(1, "name1", "description1", LocalDate.now(), 60, new Mpa(1, "G"));
    private final Film film2 = new Film(2, "name2", "description2", LocalDate.now(), 60, new Mpa(2, "PG"));
    private final Film film3 = new Film(3, "name3", "description3", LocalDate.now(), 60, new Mpa(3, "PG-13"));

    private final User user1 = new User(1, "test1@mail.ru", "Login1", "Name1", LocalDate.now());
    private final User user2 = new User(2, "test2@mail.ru", "Login2", "Name2", LocalDate.now());
    private final User user3 = new User(3, "test3@mail.ru", "Login3", "Name3", LocalDate.now());

    @BeforeEach
    void setup() {
        film1.addGenre(new Genre(1, "Комедия"));
        film1.addGenre(new Genre(3, "Мультфильм"));
        film1.addGenre(new Genre(5, "Документальный"));

        film2.addGenre(new Genre(3, "Мультфильм"));

        film3.addGenre(new Genre(5, "Документальный"));

        filmDbStorage.save(film1);
        filmDbStorage.save(film2);
        filmDbStorage.save(film3);
    }

    @AfterEach
    void clear() {
        jdbc.update("DELETE FROM film_genre");
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_director");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
    }

    @Test
    void getAll() {
        userDbStorage.save(user1);
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
        userDbStorage.save(user1);
        final Film film4 = new Film(4, "name4", "description4", LocalDate.now(), 60, new Mpa(1, "G"));
        film4.addGenre(new Genre(5, "Документальный"));
        filmDbStorage.save(film4);
        filmDbStorage.saveId(film4.getId(), user1.getId());
        final Film film = filmDbStorage.getById(film4.getId());

        assertThat(film).hasFieldOrPropertyWithValue("id", film4.getId());
        assertThat(film).hasFieldOrPropertyWithValue("name", film4.getName());
        assertThat(film).hasFieldOrPropertyWithValue("description", film4.getDescription());
        assertThat(film).hasFieldOrPropertyWithValue("releaseDate", film4.getReleaseDate());
        assertThat(film).hasFieldOrPropertyWithValue("duration", film4.getDuration());

        assertThat(film.getMpa().getId()).isEqualTo(film4.getMpa().getId());
        assertThat(film.getMpa().getName()).isEqualTo(film4.getMpa().getName());
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
        film1.setMpa(new Mpa(5, "NC-17"));
        film1.addGenre(new Genre(2, "Драма"));

        filmDbStorage.saveUpdatedObject(film1);

        final Film updFilm = filmDbStorage.getById(film1.getId());

        assertThat(updFilm).hasFieldOrPropertyWithValue("id", film1.getId());
        assertThat(updFilm).hasFieldOrPropertyWithValue("name", film1.getName());
        assertThat(updFilm).hasFieldOrPropertyWithValue("description", film1.getDescription());
        assertThat(updFilm).hasFieldOrPropertyWithValue("releaseDate", film1.getReleaseDate());
        assertThat(updFilm).hasFieldOrPropertyWithValue("duration", film1.getDuration());

        assertThat(updFilm.getMpa().getId()).isEqualTo(film1.getMpa().getId());
        assertThat(updFilm.getMpa().getName()).isEqualTo(film1.getMpa().getName());
        assertThat(updFilm.getGenres().size()).isEqualTo(film1.getGenres().size());
    }

    @Test
    void saveIdAndRemoveId() {
        userDbStorage.save(user1);
        final Film film4 = new Film(4, "name4", "description4", LocalDate.now(), 60, new Mpa(1, "G"));
        filmDbStorage.save(film4);
        Film film = filmDbStorage.saveId(film4.getId(), user1.getId());
        assertThat(film.getIds().size()).isEqualTo(1);
        assertThat(film.getIds().contains(user1.getId())).isEqualTo(true);

        film = filmDbStorage.removeId(film4.getId(), user1.getId());
        assertThat(film.getIds().size()).isEqualTo(0);
    }


    @Test
    void findTheMostPopular() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);
        userDbStorage.save(user3);

        filmDbStorage.saveId(film3.getId(), user1.getId());
        filmDbStorage.saveId(film3.getId(), user2.getId());
        filmDbStorage.saveId(film3.getId(), user3.getId());

        filmDbStorage.saveId(film2.getId(), user1.getId());
        filmDbStorage.saveId(film2.getId(), user2.getId());

        filmDbStorage.saveId(film1.getId(), user1.getId());

        // проверка самых популярных фильмов без указания genreId и year
        final List<Film> mostPopular = filmDbStorage.findTheMostPopular(3, null, null).stream().toList();

        assertThat(mostPopular.get(0)).hasFieldOrPropertyWithValue("id", film3.getId());
        assertThat(mostPopular.get(1)).hasFieldOrPropertyWithValue("id", film2.getId());
        assertThat(mostPopular.get(mostPopular.size() - 1)).hasFieldOrPropertyWithValue("id", film1.getId());

        final Film film = mostPopular.get(0);

        assertThat(film).hasFieldOrPropertyWithValue("id", film3.getId());
        assertThat(film).hasFieldOrPropertyWithValue("name", film3.getName());
        assertThat(film).hasFieldOrPropertyWithValue("description", film3.getDescription());
        assertThat(film).hasFieldOrPropertyWithValue("releaseDate", film3.getReleaseDate());
        assertThat(film).hasFieldOrPropertyWithValue("duration", film3.getDuration());
        assertThat(film.getMpa().getId()).isEqualTo(film3.getMpa().getId());
        assertThat(film.getMpa().getName()).isEqualTo(film3.getMpa().getName());
        assertThat(film.getGenres().size()).isEqualTo(film3.getGenres().size());
        assertThat(film.getIds().size()).isEqualTo(3);

        // проверка самых популярных фильмов без указания genreId
        final List<Film> mostPopularWithYear = filmDbStorage.findTheMostPopular(3, null, 2025).stream().toList();

        assertThat(mostPopularWithYear.get(0)).hasFieldOrPropertyWithValue("id", film3.getId());
        assertThat(mostPopularWithYear.get(1)).hasFieldOrPropertyWithValue("id", film2.getId());
        assertThat(mostPopularWithYear.get(mostPopular.size() - 1)).hasFieldOrPropertyWithValue("id", film1.getId());

        final List<Film> mostPopularWithYear2 = filmDbStorage.findTheMostPopular(3, null, 1000).stream().toList();
        assertThat(mostPopularWithYear2.size()).isEqualTo(0);

        // проверка самых популярных фильмов без указания year
        final List<Film> mostPopularWithGenre = filmDbStorage.findTheMostPopular(3, 5, null).stream().toList();
        assertThat(mostPopularWithGenre.get(0)).hasFieldOrPropertyWithValue("id", film3.getId());
        assertThat(mostPopularWithGenre.get(1)).hasFieldOrPropertyWithValue("id", film1.getId());
        assertThat(mostPopularWithGenre.size()).isEqualTo(2);

        final List<Film> mostPopularWithGenre2 = filmDbStorage.findTheMostPopular(3, 1000, null).stream().toList();
        assertThat(mostPopularWithGenre2.size()).isEqualTo(0);

        // проверка самых популярных фильмов c указанием всех параметров
        final List<Film> mostPopularWithGenreAndYear = filmDbStorage.findTheMostPopular(3, 3, 2025).stream().toList();
        assertThat(mostPopularWithGenreAndYear.get(0)).hasFieldOrPropertyWithValue("id", film2.getId());
        assertThat(mostPopularWithGenreAndYear.get(1)).hasFieldOrPropertyWithValue("id", film1.getId());
        assertThat(mostPopularWithGenreAndYear.size()).isEqualTo(2);
    }

    @Test
    void findRecommendations() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);
        userDbStorage.save(user3);

        // добавление лайков, при этом для user1 в рекомендациях должны оказаться фильмы с id 2, 3
        filmDbStorage.saveId(film1.getId(), user1.getId());
        filmDbStorage.saveId(film1.getId(), user2.getId());
        filmDbStorage.saveId(film2.getId(), user2.getId());
        filmDbStorage.saveId(film1.getId(), user3.getId());
        filmDbStorage.saveId(film2.getId(), user3.getId());
        filmDbStorage.saveId(film3.getId(), user3.getId());

        final List<Film> recommendations = filmDbStorage.findRecommendations(1).stream().toList();
        assertThat(recommendations.get(0)).hasFieldOrPropertyWithValue("id", film2.getId());
        assertThat(recommendations.get(1)).hasFieldOrPropertyWithValue("id", film3.getId());
        assertThat(recommendations.size()).isEqualTo(2);

        //для фильма 3 не должно быть рекомендаций
        final List<Film> recommendations1 = filmDbStorage.findRecommendations(3).stream().toList();
        assertThat(recommendations1.size()).isEqualTo(0);
    }
}