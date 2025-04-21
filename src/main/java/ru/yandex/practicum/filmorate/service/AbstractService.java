package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.StorageData;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Collection;

abstract class AbstractService<T extends StorageData> {

    private long id = 0L;

    protected Storage<T> storage;

    public Collection<T> findAll() {
        return storage.getAll();
    }

    public T create(T object) {
        object.setId(++id);
        storage.save(object);
        return object;
    }

    abstract T update(T newObject);

    public T findById(long id) {
        return storage.getById(id);
    }

    public T saveId(long id, long idForSave) {
        final T object = storage.getById(id);
        object.saveId(idForSave);
        return object;
    }

    public T removeId(long id, long idForRemove) {
        final T object = storage.getById(id);
        object.deleteId(idForRemove);
        return object;
    }

    protected boolean isNotNullAndIsNotBlank(String field) {
        return field != null && !field.isBlank();
    }

}
