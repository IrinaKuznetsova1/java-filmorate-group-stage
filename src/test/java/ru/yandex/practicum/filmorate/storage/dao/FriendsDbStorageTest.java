package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.mappers.FriendsRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class,
        FriendsDbStorage.class, FriendsRowMapper.class})
class FriendsDbStorageTest {
    private final FriendsDbStorage friendsDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbc;

    private final User user1 = new User(1, "test1@mail.ru", "Login1", "Name1", LocalDate.now());
    private final User user2 = new User(2, "test2@mail.ru", "Login2", "Name2", LocalDate.now());
    private final User user3 = new User(3, "test3@mail.ru", "Login3", "Name3", LocalDate.now());

    @BeforeEach
    void setup() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);
        friendsDbStorage.saveId(user1.getId(), user2.getId());
    }

    @AfterEach
    void clear() {
        jdbc.update("DELETE FROM friends");
        jdbc.update("DELETE FROM users");
    }

    @Test
    void getAllFriendsById() {
        final Collection<Long> friendsEmpty = friendsDbStorage.getAllFriendsById(100);
        assertTrue(friendsEmpty.isEmpty());

        final Collection<Long> friends = friendsDbStorage.getAllFriendsById(user1.getId());
        assertThat(friends.size()).isEqualTo(1);
        assertTrue(friends.contains(user2.getId()));
    }

    @Test
    void getByFriendsId() {
        final Optional<Long> friend = friendsDbStorage.getByFriendsId(user1.getId(), user2.getId());
        assertThat(friend).isPresent();
        assertThat(friend.get()).isEqualTo(user2.getId());

        final Optional<Long> friendEmpty = friendsDbStorage.getByFriendsId(200, user2.getId());
        assertThat(friendEmpty).isEmpty();
    }

    @Test
    void saveId() {
        friendsDbStorage.saveId(user1.getId(), user3.getId());
        assertThat(friendsDbStorage.getByFriendsId(user1.getId(), user3.getId())).isPresent();

        assertThrows(DuplicatedDataException.class, () -> friendsDbStorage.saveId(user1.getId(), user3.getId()));
    }

    @Test
    void removeId() {
        friendsDbStorage.removeId(user1.getId(), user2.getId());
        assertTrue(friendsDbStorage.getAllFriendsById(user1.getId()).isEmpty());
    }
}