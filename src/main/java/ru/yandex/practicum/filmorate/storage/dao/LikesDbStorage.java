package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.storage.dao.mappers.LikeRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class LikesDbStorage extends BaseDbStorage<Long> {

    private static final String FIND_ALL_BY_ID_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String FIND_LIKE_QUERY = "SELECT user_id FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String INSERT_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    @Autowired
    public LikesDbStorage(JdbcTemplate jdbc, LikeRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    protected long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);
        return 1;
    }

    public Collection<Long> getAllByFilmId(long id) {
        return findMany(FIND_ALL_BY_ID_QUERY, id);
    }

    public Optional<Long> getByIds(long filmId, long userId) {
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
