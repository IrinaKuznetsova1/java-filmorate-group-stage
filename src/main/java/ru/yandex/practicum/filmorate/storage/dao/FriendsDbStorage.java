package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.storage.dao.mappers.FriendsRowMapper;

import java.util.*;

@Slf4j
@Repository
public class FriendsDbStorage extends BaseDbStorage<LineData> {

    private static final String FIND_ALL_BY_ID_QUERY = "SELECT * FROM friends WHERE user_id = ?";
    private static final String FIND_FRIENDS_QUERY = "SELECT * FROM friends WHERE user_id = ? AND friend_id = ?";

    private static final String INSERT_QUERY = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";

    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";

    @Autowired
    public FriendsDbStorage(JdbcTemplate jdbc, FriendsRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<Long> getAllFriendsById(long id) {
        if (findMany(FIND_ALL_BY_ID_QUERY, id).isEmpty())
            return Collections.emptyList();
        return findMany(FIND_ALL_BY_ID_QUERY, id)
                .stream()
                .map(LineData::getId2)
                .toList();
    }

    public Optional<LineData> getByFriendsId(long userId, long friendId) {
        return findOne(FIND_FRIENDS_QUERY, userId, friendId);
    }

    public void saveId(long userId, long friendId) {
        if (getByFriendsId(userId, friendId).isPresent())
            throw new DuplicatedDataException("Пользователь " + userId + " уже добавил в друзья пользователя " + friendId, "friend_id");
        insert(INSERT_QUERY, userId, friendId);
        log.info("Объект сохранен в таблицу friends.");
    }


    public void removeId(long userId, long friendId) {
        if (getByFriendsId(userId, friendId).isPresent())
            delete(DELETE_FRIEND_QUERY, userId, friendId);
        log.info("Объект удален из таблицы friends.");
    }
}


