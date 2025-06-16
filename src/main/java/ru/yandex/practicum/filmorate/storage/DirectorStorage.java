package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {
    Collection<Director> getAll();

    Optional<Director> getById(long id);

    Director save(Director director);

    Director update(Director director);

    void delete(long id);
}