package ru.yandex.practicum.filmorate.storage.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.UserEventFeed;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserEventFeedRowMapper implements RowMapper<UserEventFeed> {
    @Override
    public UserEventFeed mapRow(ResultSet rs, int rowNum) throws SQLException {

        UserEventFeed userEventFeed = new UserEventFeed();

        userEventFeed.setEventId(rs.getLong("event_id"));
        userEventFeed.setUserId(rs.getLong("user_id"));
        userEventFeed.setTimestamp(rs.getLong("timeline"));
        userEventFeed.setEventType(rs.getString("event_type_name"));
        userEventFeed.setOperation(rs.getString("operation_name"));
        userEventFeed.setEntityId(rs.getLong("entity_id"));

        return userEventFeed;
    }
}
