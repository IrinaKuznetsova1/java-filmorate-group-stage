package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage extends Storage<User> {
    boolean emailIsDuplicated(String email);

    Collection<User> findUsersFriends(long id);

    Collection<User> findCommonFriends(long id, long otherId);

    void delete(long userId);
}
