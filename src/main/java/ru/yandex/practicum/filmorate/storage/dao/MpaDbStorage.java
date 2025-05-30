package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.mappers.MpaRowMapper;

import java.util.Collection;

@Repository
public class MpaDbStorage extends BaseDbStorage<Mpa> {

    private static final String FIND_ALL_QUERY = "SELECT * FROM MPA ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM MPA WHERE id = ?";

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbc, MpaRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<Mpa> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Mpa getById(long id) {
        if (findOne(FIND_BY_ID_QUERY, id).isEmpty())
            throw new NotFoundException("Возрастной рейтинг с id: " + id + " не найден.");
        return findOne(FIND_BY_ID_QUERY, id).get();
    }

}
