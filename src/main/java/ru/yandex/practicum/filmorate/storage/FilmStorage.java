package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage extends Storage<Film> {
    Collection<Film> findTheMostPopular(long count, Integer genreId, Integer year);

    Collection<Film> getFilmsByDirectorSortedByLikes(long directorId);

    Collection<Film> getFilmsByDirectorSortedByYear(long directorId);

    void delete(long filmId);

    Collection<Film> findByFilmNameAndOrDirectorAndBackPopularFilms(String query, String by);

    Collection<Film> findCommonFilms(long userId, long friendId);
}