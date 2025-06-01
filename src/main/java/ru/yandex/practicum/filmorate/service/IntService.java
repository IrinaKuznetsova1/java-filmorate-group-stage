package ru.yandex.practicum.filmorate.service;

import java.util.Collection;

public interface IntService<T> {
    Collection<T> findAll();

    T findById(long id);

    T create(T object);

    T update(T newObject);
}
