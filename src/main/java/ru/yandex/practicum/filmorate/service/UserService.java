package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
public class UserService extends Service<User> {

    @Override
    public User save(User user) {
        if (emailIsDuplicated(user.getEmail())) {
            log.warn("E-mail: {} уже используется.", user.getEmail());
            throw new DuplicatedDataException("E-mail: " + user.getEmail() + " уже используется.");
        }
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        savedObjects.put(user.getId(), user);
        log.info("Пользователь сохранен в Map<Long, User> savedObjects.");
        return user;
    }

    @Override
    public User updateFields(User newUser) {
        if (savedObjects.containsKey(newUser.getId())) {
            log.info("Пользователь найден в в Map<Long, User> savedObjects.");
            final User oldUser = savedObjects.get(newUser.getId());
            // если поля не null, то обновляем их
            if (newUser.getEmail() != null && !newUser.getEmail().isBlank() && !newUser.getEmail().equals(oldUser.getEmail())) {
                if (emailIsDuplicated(newUser.getEmail())) {
                    log.warn("E-mail: {} уже используется.", newUser.getEmail());
                    throw new DuplicatedDataException("E-mail: " + newUser.getEmail() + " уже используется.");
                }
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getLogin() != null && !newUser.getLogin().isBlank() && !newUser.getLogin().contains(" ")) {
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getName() != null && !newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
            }
            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            log.info("Обновление пользователя завершено.");
            return oldUser;
        } else {
            log.warn("Выброшено исключение NotFoundException, пользователь с id:{} не найден.", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден.");
        }
    }

    private boolean emailIsDuplicated(String email) {
        return savedObjects.values()
                .stream()
                .map(User::getEmail)
                .anyMatch(str -> str.equals(email));
    }
}
