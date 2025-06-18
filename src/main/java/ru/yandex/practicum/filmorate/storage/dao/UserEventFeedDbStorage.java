package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.UserEventFeed;
import ru.yandex.practicum.filmorate.storage.dao.mappers.UserEventFeedRowMapper;

import java.time.Instant;
import java.util.Collection;

@Slf4j
@Repository
public class UserEventFeedDbStorage extends BaseDbStorage<UserEventFeed> {

    @Autowired
    public UserEventFeedDbStorage(JdbcTemplate jdbc, UserEventFeedRowMapper mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_EVENT_FEED_OF_USER_BY_ID = "SELECT " +
            "uef.event_id, " +
            "uef.user_id, " +
            "uef.timeline, " +
            "uef.event_type, " +
            "uef.operation, " +
            "uef.entity_id " +
            "FROM userEventFeed AS uef " +
            "WHERE uef.user_id = ? " +
            "ORDER BY uef.timeline";
    private static final String INSERT_EVENT = "INSERT INTO userEventFeed (user_id, timeline, event_type, operation, entity_id) " +
            "VALUES (?, ?, ?, ?, ?)";

    public Collection<UserEventFeed> showEventFeedOfUser(long id) {
        log.info("Отправляем запрос в репозиторий");
        return findMany(FIND_EVENT_FEED_OF_USER_BY_ID, id);
    }

    public long addEvent(Long userId, Long entityId, UserEventFeed.EventType eventType, UserEventFeed.Operation operation) {
        log.info("Сохраняем, что пользователь с id {} совершил {} {} сущности с id {}", userId, operation, eventType, entityId);
        return insert(INSERT_EVENT, userId, Instant.now().toEpochMilli(), eventType.toString(), operation.toString(), entityId);
    }
}
