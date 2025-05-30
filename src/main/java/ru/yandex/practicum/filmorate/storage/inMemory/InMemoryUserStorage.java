package ru.yandex.practicum.filmorate.storage.inMemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage extends AbstractInMemoryStorage<User> implements UserStorage {
    @Override
    public boolean emailIsDuplicated(String email) {
        return getAll()
                .stream()
                .map(User::getEmail)
                .anyMatch(str -> str.equals(email));
    }

    @Override
    public Collection<User> findUsersFriends(long id) {
        return getById(id).getIds()
                .stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> findCommonFriends(long id, long otherId) {
        return getById(id).getIds()
                .stream()
                .filter(getById(otherId).getIds()::contains)
                .map(this::getById)
                .toList();
    }

}
