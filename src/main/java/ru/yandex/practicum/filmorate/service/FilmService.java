package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;

@Service
@Slf4j
public class FilmService extends AbstractService<Film> {
    private final UserStorage userStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        storage = filmStorage;
        this.userStorage = userStorage;
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
        log.info("Обновление фильма завершено.");
        return oldFilm;
    }

    public Film addLike(long id, long userId) {
        userStorage.getById(userId); //если пользователь не найден, то AbstractStorage.getById(long id) выбросит исключение
        final Film film = saveId(id, userId); // если film с {id} не найден, то AbstractStorage.getById(long id) выбросит исключение
        log.info("Пользователь id {} поставил лайк фильму id {}.", userId, id);
        return film;
    }

    public Film deleteLike(long id, long userId) {
        userStorage.getById(userId); //если пользователь не найден, то AbstractStorage.getById(long id) выбросит исключение
        final Film film = removeId(id, userId); // если film с {id} не найден, то AbstractStorage.getById(long id) выбросит исключение
        log.info("Пользователь id {} удалил лайк у фильма id {}.", userId, id);
        return film;
    }

    public Collection<Film> findTheMostPopular(long count) {
        log.info("Поиск самых популярных фильмов, количество: {}.", count);
        return findAll()
                .stream()
                .sorted(Comparator.comparing(Film::getNumberOfIds).reversed())
                .limit(count)
                .toList();
    }

}
