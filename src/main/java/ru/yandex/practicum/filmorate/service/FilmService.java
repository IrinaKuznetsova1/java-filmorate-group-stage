package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j

public class FilmService implements ru.yandex.practicum.filmorate.service.Service<Film> {
    private final FilmStorage storage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, @Qualifier("userDbStorage") UserStorage userStorage) {
        this.storage = filmStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Collection<Film> findAll() {
        return storage.getAll();
    }

    @Override
    public Film findById(long id) {
        return storage.getById(id);
    }

    @Override
    public Film create(Film film) {
        return storage.save(film);
    }

    private boolean isNotNullAndIsNotBlank(String field) {
        return field != null && !field.isBlank();
    }


    @Override
    public Film update(Film newFilm) {
        final Film oldFilm = findById(newFilm.getId());
        log.info("Фильм найден в в Map<Long, Film> films.");
        // если поля не null и не 0, то обновляем их
        if (isNotNullAndIsNotBlank(newFilm.getName()))
            oldFilm.setName(newFilm.getName());
        if (isNotNullAndIsNotBlank(newFilm.getDescription()))
            oldFilm.setDescription(newFilm.getDescription());
        if (newFilm.getReleaseDate() != null)
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        if (newFilm.getDuration() > 0)
            oldFilm.setDuration(newFilm.getDuration());
        if (newFilm.getMpa() != null)
            oldFilm.setMpa(newFilm.getMpa());
        if (!newFilm.getGenres().isEmpty())
            newFilm.getGenres().forEach(oldFilm::addGenre);
        storage.saveUpdatedObject(oldFilm);
        log.info("Обновление фильма завершено.");
        return oldFilm;
    }

    public Film addLike(long filmId, long userId) {
        findById(filmId);
        userStorage.getById(userId);
        storage.saveId(filmId, userId);
        return findById(filmId);
    }

    public Film deleteLike(long filmId, long userId) {
        findById(filmId);
        userStorage.getById(userId);
        storage.removeId(filmId, userId);
        log.info("Пользователь id {} удалил лайк у фильма id {}.", userId, filmId);
        return findById(filmId);
    }

    public Collection<Film> findTheMostPopular(long count) {
        log.info("Поиск самых популярных фильмов, количество: {}.", count);
        return storage.findTheMostPopular(count);
    }

}
