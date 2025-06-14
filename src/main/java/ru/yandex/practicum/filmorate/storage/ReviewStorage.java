package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {

    Collection<Review> findReviews(Long filmId, int count);

    void remove(long reviewId);

    void saveLike(long reviewId, long userId);

    void saveDislike(long reviewId, long userId);

    void removeUserLike(long id, long userId);

    Review saveUpdatedObject(Review review);

    Review save(Review review);

    Review getById(long reviewId);
}
