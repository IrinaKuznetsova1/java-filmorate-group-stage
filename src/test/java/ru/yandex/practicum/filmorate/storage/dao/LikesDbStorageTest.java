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
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.mappers.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class, FilmRowMapper.class,
        GenreDbStorage.class, GenreRowMapper.class,
        MpaDbStorage.class, MpaRowMapper.class,
        LikesDbStorage.class, LikeRowMapper.class,
        FilmGenreDbStorage.class, FilmGenreRowMapper.class,
        UserDbStorage.class, UserRowMapper.class,
        FriendsDbStorage.class, FriendsRowMapper.class,
        FilmDirectorDbStorage.class, DirectorRowMapper.class,
        DirectorDbStorage.class
})
class LikesDbStorageTest {
    private final LikesDbStorage likesDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbc;

    private Film film1;
    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        film1 = new Film(0, "name1", "description1", LocalDate.now(), 60, new Mpa(1));
        user1 = new User(0, "test1@mail.ru", "Login1", "Name1", LocalDate.now());
        user2 = new User(0, "test2@mail.ru", "Login2", "Name2", LocalDate.now());

        film1 = filmDbStorage.save(film1);
        user1 = userDbStorage.save(user1);
        user2 = userDbStorage.save(user2);

        likesDbStorage.saveId(film1.getId(), user1.getId());
    }

    @AfterEach
    void clear() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_director");
        jdbc.update("DELETE FROM film_genre");
        jdbc.update("DELETE FROM friends");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM directors");
    }

    @Test
    void getAllByFilmId() {
        final Collection<Long> likesEmpty = likesDbStorage.getAllByFilmId(100);
        assertTrue(likesEmpty.isEmpty());

        final Collection<Long> likes = likesDbStorage.getAllByFilmId(film1.getId());
        assertThat(likes.size()).isEqualTo(1);
        assertTrue(likes.contains(user1.getId()));
    }

    @Test
    void getByIds() {
        final Optional<Long> like = likesDbStorage.getByIds(film1.getId(), user1.getId());
        assertThat(like).isPresent();
        assertThat(like.get()).isEqualTo(user1.getId());

        final Optional<Long> likeEmpty = likesDbStorage.getByIds(200, user1.getId());
        assertThat(likeEmpty).isEmpty();
    }

    @Test
    void saveId() {
        likesDbStorage.saveId(film1.getId(), user2.getId());
        assertThat(likesDbStorage.getByIds(film1.getId(), user2.getId())).isPresent();
    }

    @Test
    void removeId() {
        likesDbStorage.removeId(film1.getId(), user1.getId());
        assertTrue(likesDbStorage.getAllByFilmId(film1.getId()).isEmpty());
        assertThrows(NotFoundException.class, () -> likesDbStorage.removeId(film1.getId(), user1.getId()));
    }
}