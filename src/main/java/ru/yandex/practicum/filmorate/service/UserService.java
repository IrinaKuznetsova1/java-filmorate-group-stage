package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserEventFeedDto;
import ru.yandex.practicum.filmorate.exceptions.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.mapper.UserEventFeedDtoMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEventFeed;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserEventFeedDbStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements IntService<User> {
    private final UserStorage storage;
    private final FilmStorage filmStorage;
    private final UserEventFeedDbStorage userEventFeedDbStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage storage,
                       @Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserEventFeedDbStorage userEventFeedDbStorage) {
        this.storage = storage;
        this.filmStorage = filmStorage;
        this.userEventFeedDbStorage = userEventFeedDbStorage;
    }

    public Collection<User> findAll() {
        return storage.getAll();
    }

    @Override
    public User findById(long id) {
        return storage.getById(id);
    }

    private boolean isNotNullAndIsNotBlank(String field) {
        return field != null && !field.isBlank();
    }

    @Override
    public User create(User user) {
        if (storage.emailIsDuplicated(user.getEmail())) {
            log.warn("E-mail: {} уже используется.", user.getEmail());
            throw new DuplicatedDataException("email", "E-mail: " + user.getEmail() + " уже используется.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return storage.save(user);
    }


    @Override
    public User update(User newUser) {
        final User oldUser = findById(newUser.getId());
        log.info("Пользователь найден в users.");
        // если поля не null, то обновляем их
        if (isNotNullAndIsNotBlank(newUser.getEmail()) && !newUser.getEmail().equals(oldUser.getEmail())) {
            if (storage.emailIsDuplicated(newUser.getEmail())) {
                log.warn("E-mail: {} уже используется.", newUser.getEmail());
                throw new DuplicatedDataException("email", "E-mail: " + newUser.getEmail() + " уже используется.");
            }
            oldUser.setEmail(newUser.getEmail());
        }
        if (isNotNullAndIsNotBlank(newUser.getLogin()))
            oldUser.setLogin(newUser.getLogin());
        if (isNotNullAndIsNotBlank(newUser.getName()))
            oldUser.setName(newUser.getName());
        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }
        storage.saveUpdatedObject(oldUser);
        log.info("Обновление пользователя завершено.");
        return oldUser;
    }

    public User addFriend(long userId, long friendId) {
        if (userId == friendId) {
            log.warn("Выброшено исключение DuplicatedDataException, пользователь с id:{} не может добавить самого себя в друзья.", userId);
            throw new DuplicatedDataException("id", "Пользователь не может добавить самого себя в друзья.");
        }
        findById(userId);
        findById(friendId);
        long eventId = userEventFeedDbStorage.addEvent(userId, friendId, UserEventFeed.EventType.FRIEND, UserEventFeed.Operation.ADD);
        final User user = storage.saveId(userId, friendId);
        log.info("Пользователь id {} добавил в друзья пользователя с id {}.", userId, friendId);
        return user;
    }

    public User deleteFriend(long userId, long friendId) {
        findById(userId);
        findById(friendId);
        final User user = storage.removeId(userId, friendId);
        log.info("Пользователь id {} удалил из друзей пользователя с id {}.", userId, friendId);
        long eventId = userEventFeedDbStorage.addEvent(userId, friendId, UserEventFeed.EventType.FRIEND, UserEventFeed.Operation.REMOVE);
        return user;
    }

    public Collection<User> findUsersFriends(long id) {
        findById(id);
        log.info("Поиск друзей у пользователя: {}.", id);
        return storage.findUsersFriends(id);
    }

    public Collection<User> findCommonFriends(long id, long otherId) {
        findById(id);
        findById(otherId);
        log.info("Поиск общих друзей у пользователей: {}, {}.", id, otherId);
        return storage.findCommonFriends(id, otherId);
    }

    public void delete(long userId) {
        storage.delete(userId);
    }

    public Collection<Film> findFilmRecommendations(long userId) {
        findById(userId);
        log.info("Поиск рекомендаций для пользователей: {}.", userId);
        return filmStorage.findRecommendations(userId);
    }

    public Collection<UserEventFeedDto> showEventFeedOfUser(long id) {
        log.info("Проверяем, что пользователь существует");
        findById(id);
        log.info("Передаем запрос вывода Ленты событий пользователя в репозиторий");
        return userEventFeedDbStorage.showEventFeedOfUser(id).stream()
                .map(UserEventFeedDtoMapper::mapToUserEventFeedDto)
                .collect(Collectors.toList());
    }
}
