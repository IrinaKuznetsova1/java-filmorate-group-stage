package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.MpaDbStorage;

import java.util.Collection;

@Validated
@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class MpaController {
    private final MpaDbStorage mpaDb;

    @GetMapping
    public Collection<Mpa> findAll() {
        log.info("Получен запрос GET/mpa.");
        return mpaDb.getAll();
    }

    @GetMapping("/{id}")
    public Mpa findMpaById(@PathVariable @Min(1) long id) {
        log.info("Получен запрос GET/mpa/{}.", id);
        return mpaDb.getById(id);
    }
}
