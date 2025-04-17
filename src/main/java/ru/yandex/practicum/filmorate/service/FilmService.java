package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
public class FilmService extends Service<Film> {

    @Override
    public Film save(Film film) {
        film.setId(getNextId());
        savedObjects.put(film.getId(), film);
        log.info("Фильм сохранен в Map<Long, Film> savedObjects.");
        return film;
    }

    @Override
    public Film updateFields(Film newFilm) {
        if (savedObjects.containsKey(newFilm.getId())) {
            log.info("Фильм найден в в Map<Long, Film> savedObjects.");
            final Film oldFilm = savedObjects.get(newFilm.getId());
            // если поля не null и не 0, то обновляем их
            if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
                oldFilm.setName(newFilm.getName());
            }
            if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getReleaseDate() != null) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            if (newFilm.getDuration() > 0) {
                oldFilm.setDuration(newFilm.getDuration());
            }
            log.info("Обновление фильма завершено.");
            return oldFilm;
        } else {
            log.warn("Выброшено исключение NotFoundException, фильм с id:{} не найден.", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден.");
        }
    }
}
