package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public ReviewService(ReviewStorage reviewStorage, FilmStorage filmStorage, UserStorage userStorage) {
        this.reviewStorage = reviewStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Review> findReviews(Long filmId, int count) {
        try {
            filmStorage.getById(filmId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Фильм не найден");
        }

        return reviewStorage.findReviews(filmId, count);
    }

    public Review findById(long id) {
        return reviewStorage.getById(id);
    }

    public Review create(Review review) {
        Optional.ofNullable(review.getUserId())
                .orElseThrow(() -> new ValidationException("User ID не может быть null"));
        Optional.ofNullable(review.getFilmId())
                .orElseThrow(() -> new ValidationException("Film ID не может быть null"));
        Optional.ofNullable(review.getIsPositive())
                .orElseThrow(() -> new ValidationException("IsPositive не может быть null"));

        try {
            filmStorage.getById(review.getFilmId());
        } catch (NotFoundException e) {
            throw new NotFoundException("Фильм не найден");
        }
        try {
            userStorage.getById(review.getUserId());
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователель не найден");
        }

        return reviewStorage.save(review);
    }

    public Review update(Review review) {
        try {
            filmStorage.getById(review.getFilmId());
        } catch (NotFoundException e) {
            throw new NotFoundException("Фильм не найден");
        }
        try {
            userStorage.getById(review.getUserId());
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователель не найден");
        }

        return reviewStorage.saveUpdatedObject(review);
    }

    public void deleteReview(long reviewId) {
        reviewStorage.remove(reviewId);
    }

    public void addLike(long reviewId, long userId) {
        try {
            reviewStorage.getById(reviewId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Отзыв не найден");
        }
        try {
            userStorage.getById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователель не найден");
        }

        reviewStorage.saveLike(reviewId, userId);
    }

    public void addDislike(long reviewId, long userId) {
        try {
            reviewStorage.getById(reviewId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Отзыв не найден");
        }
        try {
            userStorage.getById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователель не найден");
        }

        reviewStorage.saveDislike(reviewId, userId);
    }

    public void removeUserLike(long reviewId, long userId) {
        try {
            reviewStorage.getById(reviewId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Отзыв не найден");
        }
        try {
            userStorage.getById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователель не найден");
        }

        reviewStorage.removeUserLike(reviewId, userId);
    }

    public void removeUserDislike(long reviewId, long userId) {
        try {
            reviewStorage.getById(reviewId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Отзыв не найден");
        }
        try {
            userStorage.getById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователель не найден");
        }

        reviewStorage.removeUserLike(reviewId, userId);
    }
}
