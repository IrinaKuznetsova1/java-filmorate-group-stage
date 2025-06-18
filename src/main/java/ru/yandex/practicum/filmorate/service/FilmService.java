package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.UserEventFeed;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserEventFeedDbStorage;

import java.util.*;

@Service
@Slf4j
public class FilmService implements IntService<Film> {
    private final FilmStorage storage;
    private final UserStorage userStorage;
    private final DirectorService directorService;
    private final UserEventFeedDbStorage userEventFeedDbStorage;

    @Autowired
    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            DirectorService directorService,
            UserEventFeedDbStorage userEventFeedDbStorage
    ) {
        this.storage = filmStorage;
        this.userStorage = userStorage;
        this.directorService = directorService;
        this.userEventFeedDbStorage = userEventFeedDbStorage;
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
        final List<Genre> genres = new ArrayList<>(film.getGenres());
        genres.sort(Comparator.comparing(Genre::getId));
        film.getGenres().clear();
        film.setGenres(new LinkedHashSet<>(genres));
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

        if (newFilm.getGenres() != null) {
            oldFilm.getGenres().clear();
            final List<Genre> newGenres = new ArrayList<>(newFilm.getGenres());
            newGenres.sort(Comparator.comparing(Genre::getId));
            oldFilm.setGenres(new LinkedHashSet<>(newGenres));
        }

        if (newFilm.getDirectors() != null) {
            oldFilm.getDirectors().clear();
            newFilm.getDirectors().forEach(oldFilm::addDirector);
        }

        return storage.saveUpdatedObject(oldFilm);
    }

    public Film addLike(long filmId, long userId) {
        log.info("Получен запрос на добавление лайка фильму {} от пользователя {}", filmId, userId);
        findById(filmId);
        userStorage.getById(userId);
        storage.saveId(filmId, userId);
        long eventId = userEventFeedDbStorage.addEvent(userId, filmId, UserEventFeed.EventType.LIKE, UserEventFeed.Operation.ADD);
        return findById(filmId);
    }

    public Film deleteLike(long filmId, long userId) {
        log.info("Получен запрос на удаление лайка у фильма {} от пользователя {}", filmId, userId);
        findById(filmId);
        userStorage.getById(userId);
        storage.removeId(filmId, userId);
        log.info("Пользователь id {} удалил лайк у фильма id {}.", userId, filmId);
        long eventId = userEventFeedDbStorage.addEvent(userId, filmId, UserEventFeed.EventType.LIKE, UserEventFeed.Operation.REMOVE);
        return findById(filmId);
    }

    public Collection<Film> findTheMostPopular(long count, Integer genreId, Integer year) {
        log.info("Поиск самых популярных фильмов, количество: {}, id жанра: {}, год: {}.", count, genreId, year);
        return storage.findTheMostPopular(count, genreId, year);
    }

    public Collection<Film> getFilmsByDirector(long directorId, String sortBy) {
        log.info("Получен запрос на получение фильмов режиссера {} с сортировкой по {}", directorId, sortBy);
        directorService.findById(directorId);

        return switch (sortBy.toLowerCase()) {
            case "likes" -> storage.getFilmsByDirectorSortedByLikes(directorId);
            case "year" -> storage.getFilmsByDirectorSortedByYear(directorId);
            default -> throw new ValidationException("Параметр sortBy должен быть либо 'likes', либо 'year'");
        };
    }

    public Collection<Film> findCommonFilms(long userId, long friendId) {
        log.info("Получен запрос на поиск общих фильмов пользователей {} и {}", userId, friendId);
        userStorage.getById(userId);
        userStorage.getById(friendId);
        return storage.findCommonFilms(userId, friendId);
    }

    public void delete(long filmId) {
        log.info("Получен запрос на удаление фильма с id {}", filmId);
        storage.delete(filmId);
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

    public Collection<Film> findByFilmNameAndOrDirectorAndBackPopularFilms(String query, String by) {
        if (!isNotNullAndIsNotBlank(query)) {
            log.info("query = null или пустой, возвращаем пустой список");
            return Collections.emptyList();
        }
        log.info("Отправляем запрос на возврат фильмов из хранилища по результатам поиска по тексту = {} по названию" +
                "и/или автору = {}, отсортированных по кол-ву лайков", query, by);
        return storage.findByFilmNameAndOrDirectorAndBackPopularFilms(query, by);
    }

    private boolean isNotNullAndIsNotBlank(String field) {
        return field != null && !field.isBlank();
    }
}