package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.mappers.FilmGenreRowMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Repository
public class FilmGenreDbStorage extends BaseDbStorage<LineData> {
    private final GenreDbStorage genreDb;

    private static final String FIND_ALL_BY_ID_QUERY = "SELECT * FROM film_genre WHERE film_id = ? ORDER BY genre_id";
    private static final String FIND_FILM_GENRE_QUERY = "SELECT * FROM film_genre WHERE film_id = ? AND genre_id = ?";

    private static final String INSERT_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

    private static final String DELETE_FILM_GENRE_QUERY = "DELETE FROM film_genre WHERE film_id = ?";

    @Autowired
    public FilmGenreDbStorage(JdbcTemplate jdbc, FilmGenreRowMapper mapper, GenreDbStorage genreDb) {
        super(jdbc, mapper);
        this.genreDb = genreDb;
    }

    public Collection<Genre> getAllByFilmId(long id) {
        if (findMany(FIND_ALL_BY_ID_QUERY, id).isEmpty())
            return Collections.emptyList();
        return findMany(FIND_ALL_BY_ID_QUERY, id)
                .stream()
                .map(lineData -> genreDb.getById(lineData.getId2()))
                .toList();
    }


    public Optional<LineData> getByIds(long filmId, long genreId) {
        return findOne(FIND_FILM_GENRE_QUERY, filmId, genreId);
    }

    public void saveId(long filmId, long genreId) {
        if (getByIds(filmId, genreId).isPresent())
            throw new DuplicatedDataException("Жанр с id: " + genreId + " уже добавлен к фильму: " + filmId, "genreId");
        insert(INSERT_QUERY, filmId, genreId);
        log.info("Объект сохранен в таблицу film_genre.");
    }

    public void removeGenresByFilmId(long filmId) {
        jdbc.update(DELETE_FILM_GENRE_QUERY, filmId);
        log.info("Все жанры фильма с id: {} film_genre.", filmId);
    }
}
