package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;

import java.util.Collection;

@Validated
@RestController
@RequestMapping("/genres")
@Slf4j
@RequiredArgsConstructor
public class GenreController {
    private final GenreDbStorage genreDb;

    @GetMapping
    public Collection<Genre> findAll() {
        log.info("Получен запрос GET/genres.");
        return genreDb.getAll();
    }

    @GetMapping("/{id}")
    public Genre findGenreById(@PathVariable @Min(1) long id) {
        log.info("Получен запрос GET/genres/{}.", id);
        return genreDb.getById(id);
    }
}
