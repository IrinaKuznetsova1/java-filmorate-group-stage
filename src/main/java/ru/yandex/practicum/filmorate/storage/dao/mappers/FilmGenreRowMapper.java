package ru.yandex.practicum.filmorate.storage.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.dao.LineData;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmGenreRowMapper implements RowMapper<LineData> {
    @Override
    public LineData mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new LineData(
                resultSet.getLong("id"),
                resultSet.getLong("film_id"),
                resultSet.getLong("genre_id")
        );
    }
}
