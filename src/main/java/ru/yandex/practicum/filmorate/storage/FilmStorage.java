package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage extends Storage<Film> {
    Collection<Film> findTheMostPopular(long count);

    Collection<Film> getFilmsByDirectorSortedByLikes(long directorId);

    Collection<Film> getFilmsByDirectorSortedByYear(long directorId);

    void delete(long filmId);
}