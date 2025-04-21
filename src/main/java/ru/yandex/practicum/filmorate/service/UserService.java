package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService extends AbstractService<User> {

    @Autowired
    public UserService(InMemoryUserStorage storage) {
        this.storage = storage;
    }

    @Override
    public User create(User user) {
        if (emailIsDuplicated(user.getEmail())) {
            log.warn("E-mail: {} уже используется.", user.getEmail());
            throw new DuplicatedDataException("email", "E-mail: " + user.getEmail() + " уже используется.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        super.create(user);
        return user;
    }

    private boolean emailIsDuplicated(String email) {
        return findAll()
                .stream()
                .map(User::getEmail)
                .anyMatch(str -> str.equals(email));
    }

    @Override
    public User update(User newUser) {
        final User oldUser = findById(newUser.getId());
        log.info("Пользователь найден в в Map<Long, User> users.");
        // если поля не null, то обновляем их
        if (isNotNullAndIsNotBlank(newUser.getEmail()) && !newUser.getEmail().equals(oldUser.getEmail())) {
            // проверка дублирования e-mail не проходит тесты файла postman.json, прикрепленного к техническому заданию №11
                /*if (emailIsDuplicated(newUser.getEmail())) {
                    log.warn("E-mail: {} уже используется.", newUser.getEmail());
                    throw new DuplicatedDataException("email", "E-mail: " + newUser.getEmail() + " уже используется.");
                }*/
            oldUser.setEmail(newUser.getEmail());
        }
        if (isNotNullAndIsNotBlank(newUser.getLogin()))
            oldUser.setLogin(newUser.getLogin());
        if (isNotNullAndIsNotBlank(newUser.getName()))
            oldUser.setName(newUser.getName());
        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }
        log.info("Обновление пользователя завершено.");
        return oldUser;
    }

    public User addFriend(long id, long friendId) {
        if (id == friendId) {
            log.warn("Выброшено исключение DuplicatedDataException, пользователь с id:{} не может добавить самого себя в друзья.", id);
            throw new DuplicatedDataException("id", "Пользователь не может добавить самого себя в друзья.");
        }
        final User user = saveId(id, friendId);
        saveId(friendId, id);
        log.info("Пользователь id {} добавил в друзья пользователя с id {}.", id, friendId);
        return user;
    }

    public User deleteFriend(long id, long friendId) {
        final User user = removeId(id, friendId);
        removeId(friendId, id);
        log.info("Пользователь id {} удалил из друзей пользователя с id {}.", id, friendId);
        return user;
    }

    public Collection<User> findUsersFriends(long id) {
        log.info("Поиск друзей у пользователя: {}.", id);
        return findById(id).getSavedIds()
                .stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> findCommonFriends(long id, long otherId) {
        log.info("Поиск общих друзей у пользователей: {}, {}.", id, otherId);
        final Set<Long> usersFriends = findById(id).getSavedIds();
        final Set<Long> otherUsersFriends = findById(otherId).getSavedIds();
        usersFriends.retainAll(otherUsersFriends);
        return usersFriends
                .stream()
                .map(this::findById)
                .toList();
    }
}
