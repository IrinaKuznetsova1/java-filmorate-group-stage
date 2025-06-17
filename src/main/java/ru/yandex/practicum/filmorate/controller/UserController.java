package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.annotations.Marker;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Validated
@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос GET/users.");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable @Min(1) long id) {
        log.info("Получен запрос GET/users/{}.", id);
        return userService.findById(id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> findUsersFriends(@PathVariable @Min(1) long id) {
        log.info("Получен запрос GET/users/{}/friends.", id);
        return userService.findUsersFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> findCommonFriends(@PathVariable @Min(1) long id, @PathVariable @Min(1) long otherId) {
        log.info("Получен запрос GET/users/{}/friends/common/{}.", id, otherId);
        return userService.findCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> findFilmRecommendations(@PathVariable @Min(1) long id) {
        log.info("Получен запрос GET/users/{}/recommendations.", id);
        return userService.findFilmRecommendations(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Validated({Marker.OnCreate.class})
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос POST/users.");
        return userService.create(user);
    }

    @PutMapping
    @Validated({Marker.OnUpdate.class})
    public User update(@Valid @RequestBody User newUser) {
        log.info("Получен запрос PUT/users.");
        return userService.update(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable @Min(1) long id, @PathVariable @Min(1) long friendId) {
        log.info("Получен запрос PUT/users/{}/friends/{}.", id, friendId);
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable @Min(1) long id, @PathVariable @Min(1) long friendId) {
        log.info("Получен запрос DELETE/users/{}/friends/{}.", id, friendId);
        return userService.deleteFriend(id, friendId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") long id) {
        userService.delete(id);
    }
}

