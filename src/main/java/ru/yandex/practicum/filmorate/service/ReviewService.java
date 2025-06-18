package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.UserEventFeed;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserEventFeedDbStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final UserEventFeedDbStorage userEventFeedDbStorage;

    public ReviewService(ReviewStorage reviewStorage, FilmStorage filmStorage, UserStorage userStorage,
                         UserEventFeedDbStorage userEventFeedDbStorage) {
        this.reviewStorage = reviewStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.userEventFeedDbStorage = userEventFeedDbStorage;
    }

    public Collection<Review> findReviews(Long filmId, int count) {
        Collection<Review> reviews;
        if (filmId == null) {
            reviews = reviewStorage.findReviews(filmId, count);
        } else {
            try {
                filmStorage.getById(filmId);
                reviews = reviewStorage.findReviews(filmId, count);
            } catch (NotFoundException e) {
                throw new NotFoundException("Фильм не найден");
            }
        }

        return reviews;
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
        Optional.ofNullable(review.getContent())
                .orElseThrow(() -> new ValidationException("Content не может быть null"));

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
        Review createdReview = reviewStorage.save(review);
        userEventFeedDbStorage.addEvent(createdReview.getUserId(), createdReview.getReviewId(),
                UserEventFeed.EventType.REVIEW, UserEventFeed.Operation.ADD);
        return createdReview;
    }

    public Review update(Review review) {
        Optional.ofNullable(review.getUserId())
                .orElseThrow(() -> new ValidationException("User ID не может быть null"));
        Optional.ofNullable(review.getFilmId())
                .orElseThrow(() -> new ValidationException("Film ID не может быть null"));
        Optional.ofNullable(review.getIsPositive())
                .orElseThrow(() -> new ValidationException("IsPositive не может быть null"));
        Optional.ofNullable(review.getContent())
                .orElseThrow(() -> new ValidationException("Content не может быть null"));

        Optional.ofNullable(filmStorage.getById(review.getFilmId()))
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
        Optional.ofNullable(userStorage.getById(review.getUserId()))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Review updatedReview = reviewStorage.saveUpdatedObject(review);
        userEventFeedDbStorage.addEvent(updatedReview.getUserId(), updatedReview.getReviewId(),
                UserEventFeed.EventType.REVIEW, UserEventFeed.Operation.UPDATE);
        return updatedReview;
    }

    public void deleteReview(long reviewId) {
        Review review = findById(reviewId);
        userEventFeedDbStorage.addEvent(review.getUserId(), reviewId,
                UserEventFeed.EventType.REVIEW, UserEventFeed.Operation.REMOVE);
        reviewStorage.remove(reviewId);
    }

    public void addLike(long reviewId, long userId) {
        Optional.ofNullable(reviewStorage.getById(reviewId))
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        Optional.ofNullable(userStorage.getById(userId))
                .orElseThrow(() -> new NotFoundException("Пользователель не найден"));

        reviewStorage.saveLike(reviewId, userId);
    }

    public void addDislike(long reviewId, long userId) {
        Optional.ofNullable(reviewStorage.getById(reviewId))
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        Optional.ofNullable(userStorage.getById(userId))
                .orElseThrow(() -> new NotFoundException("Пользователель не найден"));

        reviewStorage.saveDislike(reviewId, userId);
    }

    public void removeUserLike(long reviewId, long userId) {
        Optional.ofNullable(reviewStorage.getById(reviewId))
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        Optional.ofNullable(userStorage.getById(userId))
                .orElseThrow(() -> new NotFoundException("Пользователель не найден"));

        reviewStorage.removeUserLike(reviewId, userId);
    }

    public void removeUserDislike(long reviewId, long userId) {
        Optional.ofNullable(reviewStorage.getById(reviewId))
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        Optional.ofNullable(userStorage.getById(userId))
                .orElseThrow(() -> new NotFoundException("Пользователель не найден"));

        reviewStorage.removeUserLike(reviewId, userId);
    }
}
