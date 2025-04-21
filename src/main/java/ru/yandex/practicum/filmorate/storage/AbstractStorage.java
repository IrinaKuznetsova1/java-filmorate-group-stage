package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.StorageData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractStorage<T extends StorageData> implements Storage<T> {
    protected final Map<Long, T> storage = new HashMap<>();

    @Override
    public Collection<T> getAll() {
        return storage.values();
    }

    @Override
    public void save(T object) {
        storage.put(object.getId(), object);
        log.info("Объект сохранен в storage.");
    }

    @Override
    public T getById(long id) {
        if (storage.get(id) == null) {
            log.warn("Выброшено исключение NotFoundException, объект с id:{} не найден.", id);
            throw new NotFoundException("Объект с id = " + id + " не найден.");
        }
        return storage.get(id);
    }
}
