package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.mappers.GenreRowMapper;

import java.util.Collection;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
class GenreDbStorageTest {
    private final GenreDbStorage genreDbStorage;

    @Test
    void getAll() {
        final Collection<Genre> genres = genreDbStorage.getAll();
        assertThat(genres.size()).isEqualTo(6);
    }

    @Test
    void getById() {
        final Genre genre = genreDbStorage.getById(3);
        assertThat(genre).hasFieldOrPropertyWithValue("id", 3);
    }

    @Test
    void getNameById() {
        final String genreName = genreDbStorage.getNameById(2);
        assertThat(genreName).isEqualTo("Драма");
    }
}