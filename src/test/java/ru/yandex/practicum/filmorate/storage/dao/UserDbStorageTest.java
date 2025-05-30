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
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.mappers.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class,
        FriendsDbStorage.class, FriendsRowMapper.class})
class UserDbStorageTest {
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbc;

    private final User user1 = new User(1, "test1@mail.ru", "Login1", "Name1", LocalDate.now());
    private final User user2 = new User(2, "test2@mail.ru", "Login2", "Name2", LocalDate.now());
    private final User user3 = new User(3, "test3@mail.ru", "Login3", "Name3", LocalDate.now());

    @BeforeEach
    void setup() {
        userDbStorage.save(user1);
        userDbStorage.save(user2);
        userDbStorage.save(user3);
    }

    @AfterEach
    void clear() {
        jdbc.update("DELETE FROM friends");
        jdbc.update("DELETE FROM users");
    }

    @Test
    void getAll() {
        userDbStorage.saveId(user1.getId(), user2.getId());
        userDbStorage.saveId(user2.getId(), user1.getId());
        userDbStorage.saveId(user3.getId(), user1.getId());

        final Collection<User> users = userDbStorage.getAll();

        assertThat(users.size()).isEqualTo(3);
        users.forEach(user -> assertThat(user.getIds().size()).isEqualTo(1));
    }

    @Test
    void emailIsDuplicated() {
        assertThat(userDbStorage.emailIsDuplicated(user1.getEmail())).isEqualTo(true);
        assertThat(userDbStorage.emailIsDuplicated("newtestmail@mail.ru")).isEqualTo(false);
    }

    @Test
    void getById() {
        final User user4 = new User(4, "test4@mail.ru", "Login4", "Name4", LocalDate.now());
        userDbStorage.save(user4);
        userDbStorage.saveId(user4.getId(), user1.getId());
        final User user = userDbStorage.getById(user4.getId());

        assertThat(user).hasFieldOrPropertyWithValue("id", user4.getId());
        assertThat(user).hasFieldOrPropertyWithValue("email", user4.getEmail());
        assertThat(user).hasFieldOrPropertyWithValue("login", user4.getLogin());
        assertThat(user).hasFieldOrPropertyWithValue("name", user4.getName());
        assertThat(user).hasFieldOrPropertyWithValue("birthday", user4.getBirthday());

        assertThat(user.getIds().size()).isEqualTo(1);

        assertThrows(NotFoundException.class, () -> userDbStorage.getById(100));
    }

    @Test
    void saveUpdatedObject() {
        user1.setEmail("updmail@mail.ru");
        user1.setLogin("updLogin1");
        user1.setName("updName1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));
        userDbStorage.saveId(user1.getId(), user2.getId());

        userDbStorage.saveUpdatedObject(user1);
        final User user = userDbStorage.getById(user1.getId());

        assertThat(user).hasFieldOrPropertyWithValue("id", user1.getId());
        assertThat(user).hasFieldOrPropertyWithValue("email", user1.getEmail());
        assertThat(user).hasFieldOrPropertyWithValue("login", user1.getLogin());
        assertThat(user).hasFieldOrPropertyWithValue("name", user1.getName());
        assertThat(user).hasFieldOrPropertyWithValue("birthday", user1.getBirthday());

        assertThat(user.getIds().size()).isEqualTo(1);
    }

    @Test
    void saveIdAndRemoveId() {
        final User user4 = new User(4, "test4@mail.ru", "Login4", "Name4", LocalDate.now());
        userDbStorage.save(user4);
        User user = userDbStorage.saveId(user4.getId(), user1.getId());
        assertThat(user.getIds().size()).isEqualTo(1);
        assertThat(user.getIds().contains(user1.getId())).isEqualTo(true);
        assertThrows(DuplicatedDataException.class, () -> userDbStorage.saveId(user4.getId(), user1.getId()));

        user = userDbStorage.removeId(user4.getId(), user1.getId());
        assertThat(user.getIds().size()).isEqualTo(0);
    }


    @Test
    void findUsersFriends() {
        // добавляем друзей user1
        userDbStorage.saveId(user1.getId(), user2.getId());
        userDbStorage.saveId(user1.getId(), user3.getId());
        // добавляем друга user2, чтобы проверить подгружается ли он при вызове метода userDbStorage.findUsersFriends()
        userDbStorage.saveId(user2.getId(), user1.getId());
        final List<User> friends = userDbStorage.findUsersFriends(user1.getId()).stream().toList();

        assertThat(friends.getFirst()).hasFieldOrPropertyWithValue("id", user2.getId());
        assertThat(friends.getLast()).hasFieldOrPropertyWithValue("id", user3.getId());

        final User user = friends.getFirst();
        assertThat(user).hasFieldOrPropertyWithValue("id", user2.getId());
        assertThat(user).hasFieldOrPropertyWithValue("email", user2.getEmail());
        assertThat(user).hasFieldOrPropertyWithValue("login", user2.getLogin());
        assertThat(user).hasFieldOrPropertyWithValue("name", user2.getName());
        assertThat(user).hasFieldOrPropertyWithValue("birthday", user2.getBirthday());

        assertThat(user.getIds().size()).isEqualTo(1);
    }

    @Test
    void findCommonFriends() {
        // добавляем общего друга user3
        userDbStorage.saveId(user1.getId(), user3.getId());
        userDbStorage.saveId(user2.getId(), user3.getId());
        // добавляем друга user3, чтобы проверить подгружается ли он при вызове метода userDbStorage.findCommonFriends()
        userDbStorage.saveId(user3.getId(), user1.getId());

        final User commonFriend = userDbStorage.findCommonFriends(user1.getId(), user2.getId()).stream().toList().getFirst();
        assertThat(commonFriend).hasFieldOrPropertyWithValue("id", user3.getId());
        assertThat(commonFriend).hasFieldOrPropertyWithValue("email", user3.getEmail());
        assertThat(commonFriend).hasFieldOrPropertyWithValue("login", user3.getLogin());
        assertThat(commonFriend).hasFieldOrPropertyWithValue("name", user3.getName());
        assertThat(commonFriend).hasFieldOrPropertyWithValue("birthday", user3.getBirthday());

        assertThat(commonFriend.getIds().size()).isEqualTo(1);
    }
}