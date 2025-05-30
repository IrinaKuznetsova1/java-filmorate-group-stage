package ru.yandex.practicum.filmorate.storage.inMemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;

@Component
@Slf4j
public class InMemoryFilmStorage extends AbstractInMemoryStorage<Film> implements FilmStorage {
    public Collection<Film> findTheMostPopular(long count) {
        return getAll()
                .stream()
                .sorted(Comparator.comparing(Film::getNumberOfIds).reversed())
                .limit(count)
                .toList();
    }

}
