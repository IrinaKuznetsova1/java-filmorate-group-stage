package ru.yandex.practicum.filmorate.storage.dao.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
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

        String event = rs.getString("event_type");
        switch (event) {
            case "LIKE":
                userEventFeed.setEventType(UserEventFeed.EventType.LIKE);
                break;
            case "REVIEW":
                userEventFeed.setEventType(UserEventFeed.EventType.REVIEW);
                break;
            case "FRIEND":
                userEventFeed.setEventType(UserEventFeed.EventType.FRIEND);
                break;
            default:
                throw new NotFoundException("Событие может быть только: LIKE, REVIEW, FRIEND");
        }
        String oper = rs.getString("operation");
        switch (oper) {
            case "REMOVE":
                userEventFeed.setOperation(UserEventFeed.Operation.REMOVE);
                break;
            case "ADD":
                userEventFeed.setOperation(UserEventFeed.Operation.ADD);
                break;
            case "UPDATE":
                userEventFeed.setOperation(UserEventFeed.Operation.UPDATE);
                break;
            default:
                throw new NotFoundException("Действие может быть только: REMOVE, ADD, UPDATE");
        }
        userEventFeed.setEntityId(rs.getLong("entity_id"));

        return userEventFeed;
    }
}
