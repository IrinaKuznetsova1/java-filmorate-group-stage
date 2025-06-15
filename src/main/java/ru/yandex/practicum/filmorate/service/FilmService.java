package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class FilmService implements IntService<Film> {
    private final FilmStorage storage;
    private final UserStorage userStorage;
    private final DirectorService directorService;

    @Autowired
    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            DirectorService directorService
    ) {
        this.storage = filmStorage;
        this.userStorage = userStorage;
        this.directorService = directorService;
    }

    @Override
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return storage.getAll();
    }

    @Override
    public Film findById(long id) {
        log.info("Получен запрос на получение фильма с id {}", id);
        return storage.getById(id);
    }

    @Override
    public Film create(Film film) {
        log.info("Получен запрос на создание нового фильма");
        validateDirectors(film);
        return storage.save(film);
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Получен запрос на обновление фильма с id {}", newFilm.getId());
        Film oldFilm = findById(newFilm.getId());
        validateDirectors(newFilm);

        if (isNotNullAndIsNotBlank(newFilm.getName())) {
            oldFilm.setName(newFilm.getName());
        }

        if (isNotNullAndIsNotBlank(newFilm.getDescription())) {
            oldFilm.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() > 0) {
            oldFilm.setDuration(newFilm.getDuration());
        }

        if (newFilm.getMpa() != null) {
            oldFilm.setMpa(newFilm.getMpa());
        }

        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            oldFilm.getGenres().clear();
            newFilm.getGenres().forEach(oldFilm::addGenre);
        }

        if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
            oldFilm.getDirectors().clear();
            newFilm.getDirectors().forEach(oldFilm::addDirector);
        }

        return storage.saveUpdatedObject(oldFilm);
    }

    public Film addLike(long filmId, long userId) {
        log.info("Получен запрос на добавление лайка фильму {} от пользователя {}", filmId, userId);
        findById(filmId);
        userStorage.getById(userId);
        return storage.saveId(filmId, userId);
    }

    public Film deleteLike(long filmId, long userId) {
        log.info("Получен запрос на удаление лайка у фильма {} от пользователя {}", filmId, userId);
        findById(filmId);
        userStorage.getById(userId);
        return storage.removeId(filmId, userId);
    }

    public Collection<Film> findTheMostPopular(long count) {
        log.info("Получен запрос на получение {} самых популярных фильмов", count);
        return storage.findTheMostPopular(count);
    }

    public Collection<Film> getFilmsByDirector(long directorId, String sortBy) {
        log.info("Получен запрос на получение фильмов режиссера {} с сортировкой по {}", directorId, sortBy);
        directorService.findById(directorId);

        switch (sortBy.toLowerCase()) {
            case "likes":
                return storage.getFilmsByDirectorSortedByLikes(directorId);
            case "year":
                return storage.getFilmsByDirectorSortedByYear(directorId);
            default:
                throw new ValidationException("Параметр sortBy должен быть либо 'likes', либо 'year'");
        }
    }

    private void validateDirectors(Film film) {
        if (film.getDirectors() != null) {
            film.getDirectors().forEach(director -> {
                if (director.getId() <= 0) {
                    throw new ValidationException("ID режиссера должен быть положительным числом");
                }
                directorService.findById(director.getId());
            });
        }
    }

    private boolean isNotNullAndIsNotBlank(String field) {
        return field != null && !field.isBlank();
    }
}