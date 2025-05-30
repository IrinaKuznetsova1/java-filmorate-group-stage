package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;

public interface Storage<T> {
    Collection<T> getAll();

    T save(T object);

    T getById(long id);

    T saveUpdatedObject(T object);

    T saveId(long id, long idForSave);

    T removeId(long id, long idForRm);
}

