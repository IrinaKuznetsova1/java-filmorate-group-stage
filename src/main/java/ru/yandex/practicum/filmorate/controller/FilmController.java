package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.annotations.Marker;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;

@Validated
@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос GET/films.");
        return filmService.findAll();
    }

    @GetMapping("/popular")
    public Collection<Film> findTheMostPopular(@RequestParam(defaultValue = "10") @Min(1) long count,
                                               @RequestParam(required = false) @Min(1) Integer genreId,
                                               @RequestParam(required = false) @Min(1895) Integer year) {
        log.info("Получен запрос GET/films/popular?count={}&genreId={}&year={}.", count, genreId, year);
        return filmService.findTheMostPopular(count, genreId, year);
    }

    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable @Min(1) long id) {
        log.info("Получен запрос GET/films/{}.", id);
        return filmService.findById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Validated({Marker.OnCreate.class})
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос POST/films.");
        return filmService.create(film);
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Получен запрос PUT/films.");
        return filmService.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос PUT/films/{}/like/{}.", id, userId);
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос DELETE/films/{}/like/{}.", id, userId);
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(
            @PathVariable long directorId,
            @RequestParam(defaultValue = "year") String sortBy) {
        log.info("Получен запрос GET /films/director/{}?sortBy={}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") long id) {
        filmService.delete(id);
    }

    @GetMapping("/common")
    public Collection<Film> findCommonFilms(
            @RequestParam @Min(1) long userId,
            @RequestParam @Min(1) long friendId) {
        log.info("Получен запрос GET /films/common?userId={}&friendId={}", userId, friendId);
        return filmService.findCommonFilms(userId, friendId);
    }

    @GetMapping("/search")
    public Collection<Film> findByFilmNameAndOrDirectorAndBackPopularFilms(@RequestParam String query,
                                                                           @RequestParam(defaultValue = "title")
                                                                           @Pattern(regexp = "title|director|title,director|director,title")
                                                                           String by) {
        log.info("Получен запрос GET/films/search?query={}&by={}", query, by);
        return filmService.findByFilmNameAndOrDirectorAndBackPopularFilms(query, by);
    }
}
