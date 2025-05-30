package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;

public interface Storage<T> {
    Collection<T> getAll();

    void save(T object);

    T getById(long id);
}
