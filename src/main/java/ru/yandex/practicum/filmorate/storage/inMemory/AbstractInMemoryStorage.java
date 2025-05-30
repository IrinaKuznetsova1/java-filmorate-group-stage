package ru.yandex.practicum.filmorate.storage.inMemory;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.StorageData;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AbstractInMemoryStorage<T extends StorageData> implements Storage<T> {
    protected final Map<Long, T> storage = new HashMap<>();
    private long id = 0L;

    @Override
    public Collection<T> getAll() {
        return storage.values();
    }

    @Override
    public T save(T object) {
        object.setId(++id);
        storage.put(object.getId(), object);
        log.info("Объект сохранен в storage.");
        return object;
    }

    @Override
    public T getById(long id) {
        final T object = storage.get(id);
        if (object == null) {
            log.warn("Выброшено исключение NotFoundException, объект с id:{} не найден.", id);
            throw new NotFoundException("Объект с id = " + id + " не найден.");
        }
        return object;
    }

    @Override
    public T saveUpdatedObject(T object) {
        storage.put(object.getId(), object);
        return object;
    }

    @Override
    public T saveId(long id, long idForSave) {
        final T object = getById(id);
        object.saveId(idForSave);
        return object;
    }

    @Override
    public T removeId(long id, long idForRm) {
        final T object = getById(id);
        object.deleteId(idForRm);
        return object;
    }
}
