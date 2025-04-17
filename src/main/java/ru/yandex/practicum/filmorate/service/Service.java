package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
abstract class Service<T> {
    final Map<Long, T> savedObjects = new HashMap<>();
    long id = 0L;

    long getNextId() {
        return ++id;
    }

    public Collection<T> getAll() {
        return savedObjects.values();
    }

    abstract T save(T object);

    abstract T updateFields(T object);
}
