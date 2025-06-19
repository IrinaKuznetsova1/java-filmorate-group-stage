package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class,
        GenreDbStorage.class, GenreRowMapper.class,
        MpaDbStorage.class, MpaRowMapper.class,
        FilmGenreDbStorage.class, FilmGenreRowMapper.class,
        LikesDbStorage.class, LikeRowMapper.class,
        FilmDirectorDbStorage.class, DirectorDbStorage.class, DirectorRowMapper.class})
class FilmGenreDbStorageTest {
    private final FilmGenreDbStorage filmGenreDbStorage;
    private final FilmDbStorage filmDBStorage;
    private final JdbcTemplate jdbc;
    private final GenreDbStorage genreDbStorage;

    private Film film1;
    private Genre genre1;
    private Genre genre2;
    private Genre genre3;
    private Genre genre4;

    @BeforeEach
    void setup() {
        film1 = new Film(1, "name1", "description1", LocalDate.now(), 60, new Mpa(1));
        genre1 = new Genre(1, "Комедия");
        genre2 = new Genre(3, "Мультфильм");
        genre3 = new Genre(5, "Документальный");
        genre4 = new Genre(6, "Боевик");

        genreDbStorage.getById(genre1.getId());
        genreDbStorage.getById(genre2.getId());
        genreDbStorage.getById(genre3.getId());
        genreDbStorage.getById(genre4.getId());

        film1.addGenre(genre2);
        film1.addGenre(genre3);
        film1.addGenre(genre1);

        filmDBStorage.save(film1);
    }

    @AfterEach
    void clear() {
        jdbc.update("DELETE FROM film_genre");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM film_director");
    }

    @Test
    void getAllByFilmId() {
        final Collection<Genre> genresEmpty = filmGenreDbStorage.getAllByFilmId(100);
        assertTrue(genresEmpty.isEmpty());

        final List<Genre> genres = filmGenreDbStorage.getAllByFilmId(film1.getId()).stream().toList();
        assertEquals(3, genres.size(), "Должно быть 3 жанра");

        Set<Integer> genreIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());
        assertThat(genreIds).containsExactlyInAnyOrder(genre1.getId(), genre2.getId(), genre3.getId());
    }

    @Test
    void getByIds() {
        final Optional<Integer> genre = filmGenreDbStorage.getByIds(film1.getId(), genre1.getId());
        assertThat(genre).isPresent();
        assertThat(genre.get()).isEqualTo(genre1.getId());

        final Optional<Integer> genreEmpty = filmGenreDbStorage.getByIds(200, genre1.getId());
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