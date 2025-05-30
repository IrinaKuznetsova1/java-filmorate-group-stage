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
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.mappers.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class,
        GenreDbStorage.class, GenreRowMapper.class,
        MpaDbStorage.class, MpaRowMapper.class,
        FilmGenreDbStorage.class, FilmGenreRowMapper.class,
        LikesDbStorage.class, LikeRowMapper.class})
class FilmGenreDbStorageTest {
    private final FilmGenreDbStorage filmGenreDbStorage;
    private final FilmDbStorage filmDBStorage;
    private final JdbcTemplate jdbc;

    private final Film film1 = new Film(1,"name1", "description1", LocalDate.now(), 60, new Mpa(1, "G"));
    private final Genre genre1 = new Genre(1, "Комедия");
    private final Genre genre2 = new Genre(3, "Мультфильм");
    private final Genre genre3 = new Genre(5, "Документальный");
    private final Genre genre4 = new Genre(6, "Боевик");

    @BeforeEach
    void setup() {
        film1.addGenre(genre2);
        film1.addGenre(genre3);
        film1.addGenre(genre1);

        filmDBStorage.save(film1);
    }

    @AfterEach
    void clear() {
        jdbc.update("DELETE FROM film_genre");
        jdbc.update("DELETE FROM films");
    }

    @Test
    void getAllByFilmId() {
        final Collection<Genre> genresEmpty = filmGenreDbStorage.getAllByFilmId(100);
        assertTrue(genresEmpty.isEmpty());

        final List<Genre> genres = filmGenreDbStorage.getAllByFilmId(film1.getId()).stream().toList();
        assertThat(genres.size()).isEqualTo(3);
        assertThat(genres.getFirst()).hasFieldOrPropertyWithValue("id", genre1.getId());
        assertThat(genres.get(1)).hasFieldOrPropertyWithValue("id", genre2.getId());
        assertThat(genres.getLast()).hasFieldOrPropertyWithValue("id", genre3.getId());
    }

    @Test
    void getByIds() {
        final Optional<LineData> genre = filmGenreDbStorage.getByIds(film1.getId(), genre1.getId());
        assertThat(genre).isPresent();
        assertThat(genre.get()).hasFieldOrPropertyWithValue("id1", film1.getId());
        assertThat(genre.get()).hasFieldOrPropertyWithValue("id2", genre1.getId());

        final Optional<LineData> genreEmpty = filmGenreDbStorage.getByIds(200, genre1.getId());
        assertThat(genreEmpty).isEmpty();
    }

    @Test
    void saveId() {
        filmGenreDbStorage.saveId(film1.getId(), genre4.getId());
        assertThat(filmGenreDbStorage.getByIds(film1.getId(), genre4.getId())).isPresent();

        assertThrows(DuplicatedDataException.class, () -> filmGenreDbStorage.saveId(film1.getId(), genre4.getId()));
    }

    @Test
    void removeGenresByFilmId() {
        filmGenreDbStorage.removeGenresByFilmId(film1.getId());
        assertTrue(filmGenreDbStorage.getAllByFilmId(film1.getId()).isEmpty());
    }
}