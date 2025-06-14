package ru.yandex.practicum.filmorate.storage.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Review(
                resultSet.getLong("review_id"),
                resultSet.getString("content"),
                resultSet.getBoolean("is_positive"),
                resultSet.getLong("user_id"),
                resultSet.getLong("film_id"),
                resultSet.getLong("useful"));
    }
}
