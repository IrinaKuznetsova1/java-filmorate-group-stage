package ru.yandex.practicum.filmorate.storage.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmGenreRowMapper implements RowMapper<Integer> {
    @Override
    public Integer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return resultSet.getInt("genre_id");
    }
}
