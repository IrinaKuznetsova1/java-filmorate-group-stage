package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> {

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<Genre> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Genre getById(int id) {
        Optional<Genre> genreOptional = findOne(FIND_BY_ID_QUERY, id);
        if (genreOptional.isEmpty())
            throw new NotFoundException("Жанр с id: " + id + " не найден.");
        return genreOptional.get();
    }

    public String getNameById(int id) {
        Optional<Genre> genreOptional = findOne(FIND_BY_ID_QUERY, id);
        if (genreOptional.isEmpty())
            throw new NotFoundException("Жанр с id: " + id + " не найден.");
        return genreOptional.get().getName();
    }

}
