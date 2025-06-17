package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.UserEventFeed;
import ru.yandex.practicum.filmorate.storage.dao.mappers.UserEventFeedRowMapper;

import java.time.Instant;
import java.util.Collection;

@Slf4j
@Repository
@Qualifier("userEventFeedDbStorage")
public class UserEventFeedDbStorage extends BaseDbStorage<UserEventFeed> {

    @Autowired
    public UserEventFeedDbStorage(JdbcTemplate jdbc, UserEventFeedRowMapper mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_EVENT_FEED_OF_USER_BY_ID = "SELECT " +
            "uef.event_id, " +
            "uef.user_id, " +
            "uef.timeline, " +
            "et.name AS event_type_name, " +
            "ot.name AS operation_name, " +
            "uef.entity_id " +
            "FROM userEventFeed AS uef " +
            "LEFT JOIN eventType AS et ON uef.event_type_id = et.id " +
            "LEFT JOIN operationType AS ot ON uef.operation_id = ot.id " +
            "WHERE uef.user_id = ? " +
            "ORDER BY uef.timeline DESC";
    private static final String INSERT_EVENT = "INSERT INTO userEventFeed (user_id, timeline, event_type_id, operation_id, entity_id) " +
            "VALUES (?, ?, ?, ?, ?)";

    Long timeStamp = Instant.now().toEpochMilli();

    public Collection<UserEventFeed> showEventFeedOfUser(long id) {
        log.info("Отправляем запрос в репозиторий");
        return findMany(FIND_EVENT_FEED_OF_USER_BY_ID, id);
    }

    public long addEventLikeRemove(Long userId, Long filmId) {
        log.info("Сохраняем, что пользователь с id {} удалил лайк фильму {}", userId, filmId);
        return insert(INSERT_EVENT, userId, timeStamp, 1, 1, filmId);
    }

    public long addEventLikeAdd(Long userId, Long filmId) {
        log.info("Сохраняем, что пользователь с id {} добавил лайк фильму {}", userId, filmId);
        return insert(INSERT_EVENT, userId, timeStamp, 1, 2, filmId);
    }

    public long addEventReviewRemove(Long userId, Long reviewId) {
        log.info("Сохраняем, что пользователь с id {} удалил ревью {}", userId, reviewId);
        return insert(INSERT_EVENT, userId, timeStamp, 2, 1, reviewId);
    }

    public long addEventReviewAdd(Long userId, Long reviewId) {
        log.info("Сохраняем, что пользователь с id {} добавил ревью {}", userId, reviewId);
        return insert(INSERT_EVENT, userId, timeStamp, 2, 2, reviewId);
    }

    public long addEventReviewUpdate(Long userId, Long reviewId) {
        log.info("Сохраняем, что пользователь с id {} обновил ревью {}", userId, reviewId);
        return insert(INSERT_EVENT, userId, timeStamp, 2, 3, reviewId);
    }

    public long addEventFriendRemove(Long userId, Long friendId) {
        log.info("Сохраняем, что пользователь с id {} удалил из друзей пользователя с id {}", userId, friendId);
        return insert(INSERT_EVENT, userId, timeStamp, 3, 1, friendId);
    }

    public long addEventFriendAdd(Long userId, Long friendId) {
        log.info("Сохраняем, что пользователь с id {} добавил в друзья пользователя с id {}", userId, friendId);
        return insert(INSERT_EVENT, userId, timeStamp, 3, 2, friendId);
    }
}
