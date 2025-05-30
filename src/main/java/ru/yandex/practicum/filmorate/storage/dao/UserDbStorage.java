package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.mappers.UserRowMapper;

import java.sql.Date;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("userDbStorage")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private final FriendsDbStorage friendsDb;

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String FIND_USERS_FRIENDS_QUERY = "SELECT * FROM users WHERE id IN " +
            "(SELECT friend_id from friends WHERE user_id = ?)";
    private static final String FIND_COMMON_QUERY = "SELECT * FROM users JOIN (SELECT f1.friend_id FROM friends AS f1 " +
            "JOIN friends AS f2 ON f1.friend_id = f2.friend_id WHERE f1.user_id = ? AND f2.user_id = ?) AS f " +
            "ON users.id = f.friend_id";

    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    @Autowired
    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper, FriendsDbStorage friendsDb) {
        super(jdbc, mapper);
        this.friendsDb = friendsDb;
    }

    private User addFriends(User user) {
        friendsDb.getAllFriendsById(user.getId())
                .forEach(user::saveId);
        return user;
    }

    @Override
    public Collection<User> getAll() {
        final Collection<User> users = findMany(FIND_ALL_QUERY);
        if (!users.isEmpty())
            users.forEach(this::addFriends);
        return users;
    }

    @Override
    public User save(User user) {
        final long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday())
        );
        user.setId(id);
        log.info("Объект сохранен в таблицу users.");
        return user;
    }

    @Override
    public boolean emailIsDuplicated(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email).isPresent();
    }

    @Override
    public User getById(long id) {
        final Optional<User> userOptional = findOne(FIND_BY_ID_QUERY, id);
        if (userOptional.isEmpty()) {
            log.warn("Выброшено исключение NotFoundException, пользователь с id:{} не найден.", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }
        User user = userOptional.get();
        return addFriends(user);
    }

    @Override
    public User saveUpdatedObject(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return addFriends(user);
    }

    @Override
    public User saveId(long userId, long friendId) {
        friendsDb.saveId(userId, friendId);
        return getById(userId);
    }

    @Override
    public User removeId(long id, long idForRm) {
        friendsDb.removeId(id, idForRm);
        return getById(id);
    }

    @Override
    public Collection<User> findUsersFriends(long id) {
        final Collection<User> friends = findMany(FIND_USERS_FRIENDS_QUERY, id);
        if (!friends.isEmpty())
            friends.forEach(this::addFriends);
        return friends;
    }

    @Override
    public Collection<User> findCommonFriends(long id, long otherId) {
        final Collection<User> commonFriends = findMany(FIND_COMMON_QUERY, id, otherId);
        if (!commonFriends.isEmpty())
            commonFriends.forEach(this::addFriends);
        return commonFriends;
    }

}
