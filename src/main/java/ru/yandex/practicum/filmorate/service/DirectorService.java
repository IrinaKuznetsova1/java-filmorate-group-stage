package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> findAll() {
        return directorStorage.getAll();
    }

    public Director findById(long id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id " + id + " не найден"));
    }

    public Director create(Director director) {
        return directorStorage.save(director);
    }

    public Director update(Director director) {
        findById(director.getId());
        return directorStorage.update(director);
    }

    public void delete(long id) {
        directorStorage.delete(id);
    }
}