package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage extends Storage<Film> {
    Collection<Film> findTheMostPopular(long count);

    void delete(long filmId);

    Collection<Film> findRecommendations(long userId);
}
