package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.storage.dao.mappers.LikeRowMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Repository
public class LikesDbStorage extends BaseDbStorage<LineData> {

    private static final String FIND_ALL_BY_ID_QUERY = "SELECT * FROM likes WHERE film_id = ?";
    private static final String FIND_LIKE_QUERY = "SELECT * FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String INSERT_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    @Autowired
    public LikesDbStorage(JdbcTemplate jdbc, LikeRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<Long> getAllByFilmId(long id) {
        if (findMany(FIND_ALL_BY_ID_QUERY, id).isEmpty())
            return Collections.emptyList();
        return findMany(FIND_ALL_BY_ID_QUERY, id)
                .stream()
                .map(LineData::getId2)
                .toList();
    }

    public Optional<LineData> getByIds(long filmId, long userId) {
        return findOne(FIND_LIKE_QUERY, filmId, userId);
    }

    public void saveId(long filmId, long userId) {
        if (getByIds(filmId, userId).isPresent())
            throw new DuplicatedDataException("Пользователь: " + userId + " уже поставил лайк фильму: " + filmId, "user_id");
        insert(INSERT_QUERY, filmId, userId);
        log.info("Объект сохранен в таблицу likes.");
    }

    public void removeId(long filmId, long userId) {
        if (getByIds(filmId, userId).isEmpty())
            throw new NotFoundException("Лайк пользователя id: " + userId + " фильма id: " + filmId + " не найден.");
        delete(DELETE_LIKE_QUERY, filmId, userId);
        log.info("Объект удален из таблицы likes.");
    }

}
